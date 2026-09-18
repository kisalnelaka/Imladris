package com.imladris.core.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.imladris.core.data.local.LibraryDao
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import com.imladris.core.data.local.entities.HighlightEntity
import com.imladris.core.data.local.entities.ReadingSessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val libraryDao: LibraryDao,
    @ApplicationContext private val context: Context
) {
    fun getRootFolders(): Flow<List<FolderEntity>> = libraryDao.getRootFolders()
    fun getAllFolders(): Flow<List<FolderEntity>> = libraryDao.getAllFolders()
    fun getFoldersIn(parentId: String): Flow<List<FolderEntity>> = libraryDao.getFoldersIn(parentId)
    fun getArtifactsIn(folderId: String): Flow<List<ArtifactEntity>> = libraryDao.getArtifactsIn(folderId)
    fun getRecentlyOpened(): Flow<List<ArtifactEntity>> = libraryDao.getRecentlyOpened()
    fun getRecentlyAdded(): Flow<List<ArtifactEntity>> = libraryDao.getRecentlyAdded()
    fun getRecentArtifacts(): Flow<List<ArtifactEntity>> = libraryDao.getRecentArtifacts()
    fun getAllArtifacts(): Flow<List<ArtifactEntity>> = libraryDao.getAllArtifacts()
    fun getArtifactsInProgress(): Flow<List<ArtifactEntity>> = libraryDao.getArtifactsInProgress()
    fun getUnreadArtifacts(): Flow<List<ArtifactEntity>> = libraryDao.getUnreadArtifacts()
    fun getArtifactCount(): Flow<Int> = libraryDao.getArtifactCount()
    fun getHighlights(artifactId: String): Flow<List<HighlightEntity>> = libraryDao.getHighlights(artifactId)
    fun getAllReadingSessions(): Flow<List<ReadingSessionEntity>> = libraryDao.getAllReadingSessions()
    fun getTotalReadingDurationMs(): Flow<Long?> = libraryDao.getTotalReadingDurationMs()
    fun getTotalWordsRead(): Flow<Int?> = libraryDao.getTotalWordsRead()

    suspend fun getArtifactById(id: String): ArtifactEntity? = withContext(Dispatchers.IO) {
        libraryDao.getArtifactById(id)
    }

    suspend fun getArtifactByPath(path: String): ArtifactEntity? = withContext(Dispatchers.IO) {
        libraryDao.getArtifactByPath(path)
    }

    suspend fun updateReadingProgress(
        id: String,
        lastPage: Int,
        totalPages: Int,
        progress: Float,
        durationMs: Long,
        wordsRead: Int,
        wpm: Float
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val minutes = (durationMs / 60000L).toInt()
        libraryDao.updateArtifactReadingState(
            id = id,
            lastPage = lastPage,
            totalPages = totalPages,
            progress = progress,
            lastRead = now,
            addedMinutes = minutes,
            wpm = wpm
        )
        if (durationMs > 1000L) {
            libraryDao.insertReadingSession(
                ReadingSessionEntity(
                    artifactId = id,
                    startTime = now - durationMs,
                    durationMs = durationMs,
                    wordsRead = wordsRead,
                    progressDelta = progress
                )
            )
        }
    }

    suspend fun addHighlight(
        artifactId: String,
        content: String,
        page: Int,
        color: Int,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        libraryDao.insertHighlight(
            HighlightEntity(
                artifactId = artifactId,
                content = content,
                page = page,
                timestamp = System.currentTimeMillis(),
                color = color,
                note = note
            )
        )
    }

    suspend fun deleteHighlight(id: Long) = withContext(Dispatchers.IO) {
        libraryDao.deleteHighlight(id)
    }

    suspend fun scanDirectory(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {}

        val rootDoc = DocumentFile.fromTreeUri(context, uri) ?: return@withContext
        scanRecursive(rootDoc, null)
    }

    private suspend fun scanRecursive(document: DocumentFile, parentId: String?) {
        val isDirectory = document.isDirectory || 
                         document.type == DocumentsContract.Document.MIME_TYPE_DIR ||
                         document.type == "vnd.android.document/directory"

        if (isDirectory) {
            val folderId = UUID.randomUUID().toString()
            val folder = FolderEntity(
                id = folderId,
                name = document.name ?: "Sanctuary",
                path = document.uri.toString(),
                parentId = parentId,
                glowColor = 0xFF79C0FF.toInt()
            )
            libraryDao.insertFolder(folder)
            document.listFiles().forEach { child -> scanRecursive(child, folderId) }
        } else if (document.isFile) {
            val name = document.name ?: return
            val lowerName = name.lowercase()
            if (lowerName.endsWith(".txt") || lowerName.endsWith(".pdf") || lowerName.endsWith(".epub") || lowerName.endsWith(".md")) {
                var coverPath: String? = null
                var totalPages = 1
                if (lowerName.endsWith(".pdf")) {
                    val (cover, pages) = extractPdfInfo(document.uri, name)
                    coverPath = cover
                    if (pages > 0) totalPages = pages
                }

                val artifact = ArtifactEntity(
                    id = UUID.randomUUID().toString(),
                    title = name.substringBeforeLast("."),
                    path = document.uri.toString(),
                    type = name.substringAfterLast("."),
                    coverPath = coverPath,
                    lastRead = 0L,
                    addedDate = System.currentTimeMillis(),
                    progress = 0f,
                    parentFolderId = parentId,
                    lastPage = 0,
                    totalPages = totalPages,
                    wordCount = 0,
                    readingTimeMinutes = 0,
                    readingSpeedWpm = 220f
                )
                libraryDao.insertArtifact(artifact)
            }
        }
    }

    private fun extractPdfInfo(uri: Uri, fileName: String): Pair<String?, Int> {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                var coverPath: String? = null
                if (count > 0) {
                    val page = renderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val cacheFile = File(context.cacheDir, "covers/${fileName.hashCode()}.jpg")
                    cacheFile.parentFile?.mkdirs()
                    FileOutputStream(cacheFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    page.close()
                    coverPath = cacheFile.absolutePath
                }
                renderer.close()
                Pair(coverPath, count)
            } ?: Pair(null, 1)
        } catch (e: Exception) {
            Pair(null, 1)
        }
    }

    suspend fun updateLastRead(artifact: ArtifactEntity) = withContext(Dispatchers.IO) {
        libraryDao.insertArtifact(artifact.copy(lastRead = System.currentTimeMillis()))
    }
}
