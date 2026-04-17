package com.imladris.feature.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

sealed class ReaderContent {
    data class Text(val content: String) : ReaderContent()
    data class Pdf(val pageCount: Int) : ReaderContent()
    data class Error(val message: String) : ReaderContent()
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _contentState = MutableStateFlow<ReaderContent>(ReaderContent.Text("Opening the scrolls..."))
    val contentState: StateFlow<ReaderContent> = _contentState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var pdfRenderer: PdfRenderer? = null
    private var pfd: ParcelFileDescriptor? = null

    fun loadContent(uriString: String?) {
        if (uriString == null) {
            _contentState.value = ReaderContent.Error("The scroll's location is unknown.")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uri = Uri.parse(uriString)
                
                // 1. Verify it's not a directory
                val mimeType = withContext(Dispatchers.IO) {
                    try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
                }
                
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR || uriString.endsWith("/")) {
                    _contentState.value = ReaderContent.Error("This is a sanctuary (folder), not a book. Please select a scroll from the corridors.")
                    return@launch
                }

                // 2. Load PDF or Text
                if (mimeType == "application/pdf" || uriString.lowercase().contains(".pdf")) {
                    loadPdf(uri)
                } else {
                    loadText(uri)
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
            // RELIABILITY: Always copy to a local file for seekable PdfRenderer access
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
            
            _contentState.value = ReaderContent.Pdf(renderer.pageCount)
        } catch (e: Exception) {
            _contentState.value = ReaderContent.Error("Failed to render PDF: ${e.localizedMessage}. Ensure the file is not corrupted.")
        }
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

    private suspend fun loadText(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val buffer = CharArray(1024 * 64)
                    val read = reader.read(buffer)
                    if (read != -1) {
                        _contentState.value = ReaderContent.Text(String(buffer, 0, read))
                    } else {
                        _contentState.value = ReaderContent.Text("The scroll is empty.")
                    }
                }
            } ?: run { _contentState.value = ReaderContent.Error("Unable to reach the artifact.") }
        } catch (e: Exception) {
            _contentState.value = ReaderContent.Error("Error reading archives: ${e.localizedMessage}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        pdfRenderer?.close()
        pfd?.close()
        File(context.cacheDir, "active_reader.pdf").delete()
    }
}
