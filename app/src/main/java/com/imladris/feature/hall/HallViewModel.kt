package com.imladris.feature.hall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.domain.math.RecommendationBundle
import com.imladris.core.domain.math.RecommendationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HallViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    val recentlyOpened: Flow<List<ArtifactEntity>> = repository.getRecentlyOpened()
    val recentlyAdded: Flow<List<ArtifactEntity>> = repository.getRecentlyAdded()

    val recommendations: StateFlow<RecommendationBundle> = repository.getAllArtifacts()
        .map { artifacts -> RecommendationEngine.computeRecommendations(artifacts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecommendationBundle(null, null)
        )
}
