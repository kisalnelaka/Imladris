package com.imladris.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.imladris.core.data.local.LibraryDao
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
        if (document.isDirectory) {
            val folderId = UUID.randomUUID().toString()
            val folder = FolderEntity(
                id = folderId,
                name = document.name ?: "Unknown",
                path = document.uri.toString(),
                parentId = parentId,
                glowColor = 0xFF64FFDA.toInt() // Default color
            )
            libraryDao.insertFolder(folder)
            
            document.listFiles().forEach { child ->
                scanRecursive(child, folderId)
            }
        } else {
            val name = document.name ?: return
            if (name.endsWith(".txt") || name.endsWith(".pdf") || name.endsWith(".epub")) {
                val artifact = ArtifactEntity(
                    id = UUID.randomUUID().toString(),
                    title = name,
                    path = document.uri.toString(),
                    type = name.substringAfterLast("."),
                    coverPath = null,
                    lastRead = System.currentTimeMillis(),
                    progress = 0f,
                    parentFolderId = parentId
                )
                libraryDao.insertArtifact(artifact)
            }
        }
    }
}
