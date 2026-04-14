package com.imladris

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.imladris.core.notifications.ImladrisNotifications
import com.imladris.core.notifications.MemoryRecallWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ImladrisApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        ImladrisNotifications.createChannels(this)
        scheduleMemoryRecall()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleMemoryRecall() {
        val request = PeriodicWorkRequestBuilder<MemoryRecallWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "memory_recall",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
