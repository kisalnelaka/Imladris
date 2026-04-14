package com.imladris.feature.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _currentFolderId = MutableStateFlow<String?>(null)
    val currentFolderId: StateFlow<String?> = _currentFolderId.asStateFlow()

    val rootFolders: Flow<List<FolderEntity>> = repository.getRootFolders()
    
    val recentArtifacts: Flow<List<ArtifactEntity>> = repository.getRecentArtifacts()

    fun getFoldersIn(parentId: String): Flow<List<FolderEntity>> = repository.getFoldersIn(parentId)
    
    fun getArtifactsIn(folderId: String): Flow<List<ArtifactEntity>> = repository.getArtifactsIn(folderId)

    fun scanDirectory(uri: Uri) {
        viewModelScope.launch {
            repository.scanDirectory(uri)
        }
    }

    fun navigateToFolder(id: String?) {
        _currentFolderId.value = id
    }
}
