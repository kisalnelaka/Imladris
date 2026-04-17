package com.imladris.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.imladris.core.data.local.ImladrisDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class MemoryRecallWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: ImladrisDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch real recently opened artifacts
        val artifacts = database.libraryDao().getRecentlyOpened().first()
        if (artifacts.isEmpty()) return Result.success()

        val randomArtifact = artifacts.random()
        // Highlights might be empty, so we provide a gentle reminder of the book itself
        val message = "Continue your journey in ${randomArtifact.title}"
        
        ImladrisNotifications.showGuidanceNotification(
            applicationContext,
            message
        )

        return Result.success()
    }
}
