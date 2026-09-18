package com.imladris.feature.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.HighlightEntity
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.domain.math.CircadianTheme
import com.imladris.core.domain.math.ReadingPacingEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.zip.ZipInputStream
import javax.inject.Inject

sealed class ReaderContent {
    data class Text(val pages: List<String>, val totalWords: Int) : ReaderContent()
    data class Pdf(val pageCount: Int) : ReaderContent()
    data class Error(val message: String) : ReaderContent()
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: LibraryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _contentState = MutableStateFlow<ReaderContent>(ReaderContent.Text(listOf("Opening the scrolls..."), 0))
    val contentState: StateFlow<ReaderContent> = _contentState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _isBionicReading = MutableStateFlow(false)
    val isBionicReading: StateFlow<Boolean> = _isBionicReading.asStateFlow()

    private val _readingSpeedWpm = MutableStateFlow(220f)
    val readingSpeedWpm: StateFlow<Float> = _readingSpeedWpm.asStateFlow()

    private val _circadianTheme = MutableStateFlow(
        ReadingPacingEngine.getCircadianTheme(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    )
    val circadianTheme: StateFlow<CircadianTheme> = _circadianTheme.asStateFlow()

    private val _highlights = MutableStateFlow<List<HighlightEntity>>(emptyList())
    val highlights: StateFlow<List<HighlightEntity>> = _highlights.asStateFlow()

    private var activeArtifact: ArtifactEntity? = null
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var lastPageTurnTime: Long = System.currentTimeMillis()
    private var wordsReadInSession: Int = 0

    private var pdfRenderer: PdfRenderer? = null
    private var pfd: ParcelFileDescriptor? = null
    private val bitmapCache = LruCache<Int, Bitmap>(32)

    fun loadContent(uriString: String?, widthDp: Float = 360f, heightDp: Float = 640f, fontSizeSp: Float = 18f) {
        if (uriString == null) {
            _contentState.value = ReaderContent.Error("The scroll's location is unknown.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            sessionStartTime = System.currentTimeMillis()
            lastPageTurnTime = sessionStartTime

            try {
                val uri = Uri.parse(uriString)

                // Match with database artifact directly (non-blocking single query)
                val matched = repository.getArtifactByPath(uriString)
                activeArtifact = matched
                matched?.let {
                    _readingSpeedWpm.value = if (it.readingSpeedWpm > 50f) it.readingSpeedWpm else 220f
                    _currentPage.value = it.lastPage
                }

                val mimeType = withContext(Dispatchers.IO) {
                    try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
                }

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR || uriString.endsWith("/")) {
                    _contentState.value = ReaderContent.Error("This is a sanctuary (folder), not a book.")
                    return@launch
                }

                val lower = uriString.lowercase()
                val isPdf = mimeType == "application/pdf" || lower.endsWith(".pdf") || matched?.type?.lowercase() == "pdf"
                val isEpub = mimeType == "application/epub+zip" || lower.endsWith(".epub") || matched?.type?.lowercase() == "epub"

                if (isPdf) {
                    loadPdf(uri)
                } else if (isEpub) {
                    loadEpub(uri, widthDp, heightDp, fontSizeSp)
                } else {
                    loadText(uri, widthDp, heightDp, fontSizeSp)
                }
            } catch (e: Exception) {
                _contentState.value = ReaderContent.Error("Inaccessible archives: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadPdf(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(context.cacheDir, "active_reader.pdf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                _contentState.value = ReaderContent.Error("Failed to manifest the PDF scrolls locally.")
                return@withContext
            }

            val descriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pfd = descriptor
            val renderer = PdfRenderer(descriptor)
            pdfRenderer = renderer

            _totalPages.value = renderer.pageCount
            _contentState.value = ReaderContent.Pdf(renderer.pageCount)
        } catch (e: Exception) {
            _contentState.value = ReaderContent.Error("Failed to render PDF: ${e.localizedMessage}")
        }
    }

    private suspend fun loadEpub(uri: Uri, widthDp: Float, heightDp: Float, fontSizeSp: Float) = withContext(Dispatchers.IO) {
        try {
            val textBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ZipInputStream(stream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val name = entry.name.lowercase()
                        if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                            val content = zip.bufferedReader().readText()
                            val stripped = content
                                .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                                .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                                .replace(Regex("<[^>]+>"), " ")
                                .replace("&nbsp;", " ")
                                .replace("&amp;", "&")
                                .replace("&lt;", "<")
                                .replace("&gt;", ">")
                                .replace("&quot;", "\"")
                                .replace("&#39;", "'")
                                .replace(Regex("\\s+"), " ")
                                .trim()

                            if (stripped.isNotEmpty()) {
                                textBuilder.append(stripped).append("\n\n")
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            val fullText = textBuilder.toString()
            if (fullText.isNotBlank()) {
                val pages = ReadingPacingEngine.paginateText(fullText, widthDp, heightDp, fontSizeSp)
                val words = fullText.split("\\s+".toRegex()).count { it.isNotBlank() }
                _totalPages.value = pages.size
                _contentState.value = ReaderContent.Text(pages, words)
            } else {
                loadText(uri, widthDp, heightDp, fontSizeSp)
            }
        } catch (e: Exception) {
            loadText(uri, widthDp, heightDp, fontSizeSp)
        }
    }

    private suspend fun loadText(uri: Uri, widthDp: Float, heightDp: Float, fontSizeSp: Float) = withContext(Dispatchers.IO) {
        try {
            val fullText = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }

            if (!fullText.isNullOrBlank()) {
                val pages = ReadingPacingEngine.paginateText(fullText, widthDp, heightDp, fontSizeSp)
                val words = fullText.split("\\s+".toRegex()).count { it.isNotBlank() }
                _totalPages.value = pages.size
                _contentState.value = ReaderContent.Text(pages, words)
            } else {
                _contentState.value = ReaderContent.Text(listOf("The scroll is empty."), 0)
            }
        } catch (e: Exception) {
            _contentState.value = ReaderContent.Error("Error reading archives: ${e.localizedMessage}")
        }
    }

    fun onPageChanged(newPage: Int) {
        val total = _totalPages.value
        val clamped = newPage.coerceIn(0, (total - 1).coerceAtLeast(0))
        _currentPage.value = clamped

        // Compute reading speed telemetry
        val now = System.currentTimeMillis()
        val deltaSeconds = (now - lastPageTurnTime) / 1000f
        lastPageTurnTime = now

        val estimatedWords = 250
        wordsReadInSession += estimatedWords

        val updatedWpm = ReadingPacingEngine.updateWpm(_readingSpeedWpm.value, estimatedWords, deltaSeconds)
        _readingSpeedWpm.value = updatedWpm

        persistProgress()
    }

    fun toggleBionicReading() {
        _isBionicReading.value = !_isBionicReading.value
    }

    fun setCircadianTheme(theme: CircadianTheme) {
        _circadianTheme.value = theme
    }

    suspend fun getPageBitmap(index: Int): Bitmap? = withContext(Dispatchers.Default) {
        bitmapCache.get(index)?.let { return@withContext it }
        val renderer = pdfRenderer ?: return@withContext null
        try {
            synchronized(renderer) {
                if (index < 0 || index >= renderer.pageCount) return@withContext null
                renderer.openPage(index).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapCache.put(index, bitmap)
                    bitmap
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun addBookmark(note: String = "Saved location") {
        val artifact = activeArtifact ?: return
        viewModelScope.launch {
            repository.addHighlight(
                artifactId = artifact.id,
                content = "Page ${_currentPage.value + 1}",
                page = _currentPage.value,
                color = 0xFFFFD700.toInt(),
                note = note
            )
        }
    }

    private fun persistProgress() {
        val artifact = activeArtifact ?: return
        val page = _currentPage.value
        val total = _totalPages.value
        val progress = if (total > 0) (page.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        val durationMs = System.currentTimeMillis() - sessionStartTime

        viewModelScope.launch {
            repository.updateReadingProgress(
                id = artifact.id,
                lastPage = page,
                totalPages = total,
                progress = progress,
                durationMs = durationMs,
                wordsRead = wordsReadInSession,
                wpm = _readingSpeedWpm.value
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        persistProgress()
        bitmapCache.evictAll()
        pdfRenderer?.close()
        pfd?.close()
        File(context.cacheDir, "active_reader.pdf").delete()
    }
}
