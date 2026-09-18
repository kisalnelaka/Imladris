package com.imladris.feature.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Calendar
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

                // Match with database artifact
                val all = withContext(Dispatchers.IO) {
                    // Try finding matching artifact by path
                    var matched: ArtifactEntity? = null
                    repository.getAllArtifacts().collect { list ->
                        matched = list.find { it.path == uriString }
                        if (matched != null) return@collect
                    }
                    matched
                }
                activeArtifact = all
                all?.let {
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

                if (mimeType == "application/pdf" || uriString.lowercase().contains(".pdf")) {
                    loadPdf(uri)
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

    private suspend fun loadText(uri: Uri, widthDp: Float, heightDp: Float, fontSizeSp: Float) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val sb = java.lang.StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        sb.append(line).append("\n")
                        line = reader.readLine()
                    }
                    val fullText = sb.toString()
                    val pages = ReadingPacingEngine.paginateText(fullText, widthDp, heightDp, fontSizeSp)
                    val words = fullText.split("\\s+".toRegex()).count { it.isNotBlank() }

                    _totalPages.value = pages.size
                    _contentState.value = ReaderContent.Text(pages, words)
                }
            } ?: run {
                _contentState.value = ReaderContent.Error("Unable to reach the artifact.")
            }
        } catch (e: Exception) {
            _contentState.value = ReaderContent.Error("Error reading archives: ${e.localizedMessage}")
        }
    }

    fun onPageChanged(newPage: Int) {
        val total = _totalPages.value
        val clamped = newPage.coerceIn(0, (total - 1).coerceAtLeast(0))
        val prevPage = _currentPage.value
        _currentPage.value = clamped

        // Compute reading speed telemetry
        val now = System.currentTimeMillis()
        val deltaSeconds = (now - lastPageTurnTime) / 1000f
        lastPageTurnTime = now

        // Estimate words on the read page (~250 words per page standard)
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
        val renderer = pdfRenderer ?: return@withContext null
        try {
            synchronized(renderer) {
                if (index < 0 || index >= renderer.pageCount) return@withContext null
                renderer.openPage(index).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
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
        pdfRenderer?.close()
        pfd?.close()
        File(context.cacheDir, "active_reader.pdf").delete()
    }
}
