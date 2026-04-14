package com.imladris.feature.analytics

import androidx.lifecycle.ViewModel
import com.imladris.core.data.local.LibraryDao
import com.imladris.core.data.local.entities.ArtifactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val libraryDao: LibraryDao
) : ViewModel() {

    val artifactCount: Flow<Int> = libraryDao.getArtifactCount()
    val highlightCount: Flow<Int> = libraryDao.getHighlightCount()
    val recentArtifacts: Flow<List<ArtifactEntity>> = libraryDao.getRecentArtifacts()
    
    val focusScore: Flow<Int> = libraryDao.getRecentArtifacts().map { list ->
        if (list.isEmpty()) 0 else (list.sumOf { it.progress.toDouble() * 100 } / list.size).toInt()
    }
}
