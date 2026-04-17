package com.imladris.core.data.scanner

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.imladris.core.data.local.ImladrisDatabase
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.absoluteValue

@HiltWorker
class LibraryScannerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: ImladrisDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val rootUri = inputData.getString("root_uri") ?: return Result.failure()
        val rootDoc = DocumentFile.fromTreeUri(applicationContext, android.net.Uri.parse(rootUri)) ?: return Result.failure()
        
        scanRecursive(rootDoc, null)
        
        return Result.success()
    }

    private suspend fun scanRecursive(directory: DocumentFile, parentId: String?) {
        val folderId = directory.uri.toString()
        
        // Save folder
        database.libraryDao().insertFolder(
            FolderEntity(
                id = folderId,
                name = directory.name ?: "Sanctuary",
                path = directory.uri.toString(),
                parentId = parentId,
                glowColor = folderId.hashCode().absoluteValue
            )
        )

        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                scanRecursive(file, folderId)
            } else {
                // Save artifact
                if (isSupportFileType(file.name)) {
                    database.libraryDao().insertArtifact(
                        ArtifactEntity(
                            id = file.uri.toString(),
                            title = file.name ?: "Untitled",
                            path = file.uri.toString(),
                            type = getFileType(file.name),
                            coverPath = null,
                            lastRead = 0L,
                            addedDate = System.currentTimeMillis(),
                            progress = 0f,
                            parentFolderId = folderId
                        )
                    )
                }
            }
        }
    }

    private fun isSupportFileType(name: String?): Boolean {
        return name?.let { 
            it.endsWith(".pdf", true) || it.endsWith(".epub", true) || it.endsWith(".txt", true)
        } ?: false
    }

    private fun getFileType(name: String?): String {
        return when {
            name?.endsWith(".pdf", true) == true -> "PDF"
            name?.endsWith(".epub", true) == true -> "EPUB"
            else -> "TEXT"
        }
    }
}
