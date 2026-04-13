package com.imladris.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.imladris.core.data.local.ImladrisDatabase
import com.imladris.core.notifications.ImladrisNotifications
import kotlinx.coroutines.flow.first

class MemoryRecallWorker(
    context: Context,
    params: WorkerParameters,
    private val database: ImladrisDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch a random highlight from the database
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
            // If no highlights, maybe just a reminder about the book
            ImladrisNotifications.showGuidanceNotification(
                applicationContext,
                "The halls of Imladris remind you of ${randomArtifact.title}"
            )
        }

        return Result.success()
    }
}
