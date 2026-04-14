package com.imladris.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.imladris.core.data.local.LibraryDao
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
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
    
    fun getFoldersIn(parentId: String): Flow<List<FolderEntity>> = libraryDao.getFoldersIn(parentId)
    
    fun getArtifactsIn(folderId: String): Flow<List<ArtifactEntity>> = libraryDao.getArtifactsIn(folderId)

    fun getRecentArtifacts(): Flow<List<ArtifactEntity>> = libraryDao.getRecentArtifacts()

    suspend fun scanDirectory(uri: Uri) = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, uri) ?: return@withContext
        scanRecursive(rootDoc, null)
    }

    private suspend fun scanRecursive(document: DocumentFile, parentId: String?) {
        // Strict distinction between directories and files
        if (document.isDirectory) {
            val folderId = UUID.randomUUID().toString()
            val folder = FolderEntity(
                id = folderId,
                name = document.name ?: "Unknown Sanctuary",
                path = document.uri.toString(),
                parentId = parentId,
                glowColor = 0xFF64FFDA.toInt()
            )
            libraryDao.insertFolder(folder)
            
            document.listFiles().forEach { child ->
                scanRecursive(child, folderId)
            }
        } else if (document.isFile) {
            val name = document.name ?: return
            val lowerName = name.lowercase()
            
            // Only index valid document files, excluding directories that might have extensions
            if (lowerName.endsWith(".txt") || lowerName.endsWith(".pdf") || lowerName.endsWith(".epub")) {
                
                var coverPath: String? = null
                if (lowerName.endsWith(".pdf")) {
                    coverPath = extractPdfCover(document.uri, name)
                }

                val artifact = ArtifactEntity(
                    id = UUID.randomUUID().toString(),
                    title = name,
                    path = document.uri.toString(),
                    type = name.substringAfterLast("."),
                    coverPath = coverPath,
                    lastRead = System.currentTimeMillis(),
                    progress = 0f,
                    parentFolderId = parentId
                )
                libraryDao.insertArtifact(artifact)
            }
        }
    }

    private fun extractPdfCover(uri: Uri, fileName: String): String? {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    val cacheFile = File(context.cacheDir, "covers/${fileName}.jpg")
                    cacheFile.parentFile?.mkdirs()
                    
                    FileOutputStream(cacheFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    
                    page.close()
                    renderer.close()
                    cacheFile.absolutePath
                } else {
                    renderer.close()
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
