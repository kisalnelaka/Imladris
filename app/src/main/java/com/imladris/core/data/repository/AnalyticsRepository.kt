package com.imladris.core.data.repository

import com.imladris.core.data.local.LibraryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val libraryDao: LibraryDao
) {
    /**
     * Derives a focus score (0–100) from reading progress across all artifacts.
     * A score of 100 means all indexed artifacts have been fully read.
     */
    fun getFocusScore(): Flow<Float> {
        return libraryDao.getRecentArtifacts().map { artifacts ->
            if (artifacts.isEmpty()) return@map 0f
            val average = artifacts.sumOf { it.progress.toDouble() } / artifacts.size
            (average * 100).toFloat().coerceIn(0f, 100f)
        }
    }

    /**
     * Returns a category breakdown of reading time derived from folder names.
     * Full implementation requires a ReadingSession table.
     */
    fun getReadingStats(): Flow<Map<String, Int>> {
        return libraryDao.getRootFolders().map { folders ->
            folders.associate { it.name to 0 }
        }
    }
}
