package com.imladris.feature.analytics

import androidx.lifecycle.ViewModel
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.ReadingSessionEntity
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.domain.math.RecommendationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    val artifactCount: Flow<Int> = repository.getArtifactCount()
    val recentArtifacts: Flow<List<ArtifactEntity>> = repository.getRecentlyOpened()
    val readingSessions: Flow<List<ReadingSessionEntity>> = repository.getAllReadingSessions()

    val totalReadingMinutes: Flow<Int> = repository.getTotalReadingDurationMs().map { ms ->
        ((ms ?: 0L) / 60000L).toInt()
    }

    val totalWordsRead: Flow<Int> = repository.getTotalWordsRead().map { it ?: 0 }

    val focusScore: Flow<Int> = combine(
        repository.getAllArtifacts(),
        totalReadingMinutes
    ) { artifacts, minutes ->
        RecommendationEngine.calculateFocusResonance(artifacts, minutes)
    }

    val readingStreakDays: Flow<Int> = readingSessions.map { sessions ->
        if (sessions.isEmpty()) 1
        else {
            val days = sessions.map { it.startTime / 86_400_000L }.distinct().sortedDescending()
            var streak = 1
            for (i in 0 until days.size - 1) {
                if (days[i] - days[i + 1] == 1L) streak++
                else break
            }
            streak
        }
    }
}
