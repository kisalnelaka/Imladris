package com.imladris.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.imladris.core.data.local.ImladrisDatabase
import dagger.assisted.Assisted
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import dagger.assisted.AssistedInject

@HiltWorker
class MemoryRecallWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: ImladrisDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val artifacts = database.libraryDao().getRecentArtifacts().first()
        if (artifacts.isEmpty()) return Result.success()

        val randomArtifact = artifacts.random()
        val highlights = database.libraryDao().getHighlights(randomArtifact.id).first()
        
        if (highlights.isNotEmpty()) {
            val recall = highlights.random()
            ImladrisNotifications.showRecallNotification(
                applicationContext,
                randomArtifact.title,
                recall.content
            )
        } else {
            ImladrisNotifications.showGuidanceNotification(
                applicationContext,
                "The halls of Imladris remind you of ${randomArtifact.title}"
            )
        }

        return Result.success()
    }
}
