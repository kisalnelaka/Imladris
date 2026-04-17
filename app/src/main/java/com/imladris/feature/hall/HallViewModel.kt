package com.imladris.feature.hall

import androidx.lifecycle.ViewModel
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.data.local.entities.ArtifactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HallViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    val recentlyOpened: Flow<List<ArtifactEntity>> = repository.getRecentlyOpened()
    val recentlyAdded: Flow<List<ArtifactEntity>> = repository.getRecentlyAdded()

}
