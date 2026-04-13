package com.imladris.core.domain.repository

import com.imladris.core.domain.model.Artifact
import com.imladris.core.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getRootFolders(): Flow<List<Folder>>
    fun getFolderContent(folderId: String): Flow<Folder>
    fun getRecentArtifacts(): Flow<List<Artifact>>
    suspend fun scanRootFolder(path: String)
}
