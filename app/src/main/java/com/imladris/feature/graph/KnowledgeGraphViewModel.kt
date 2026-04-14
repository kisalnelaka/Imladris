package com.imladris.feature.graph

import androidx.lifecycle.ViewModel
import com.imladris.core.data.local.LibraryDao
import com.imladris.core.data.local.entities.ArtifactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class KnowledgeGraphViewModel @Inject constructor(
    private val libraryDao: LibraryDao
) : ViewModel() {
    val artifacts: Flow<List<ArtifactEntity>> = libraryDao.getAllArtifacts()
}
