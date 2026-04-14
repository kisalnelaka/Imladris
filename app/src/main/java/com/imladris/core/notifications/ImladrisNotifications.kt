package com.imladris.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object ImladrisNotifications {
    private const val CHANNEL_GUIDANCE = "channel_guidance"
    private const val CHANNEL_RECALL = "channel_recall"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val guidance = NotificationChannel(
                CHANNEL_GUIDANCE,
                "Gentle Guidance",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for reading reminders and streaks."
            }

            val recall = NotificationChannel(
                CHANNEL_RECALL,
                "Memory Recall",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Deep highlights and past thoughts resurfacing."
            }

            manager.createNotificationChannel(guidance)
            manager.createNotificationChannel(recall)
        }
    }

    fun showRecallNotification(context: Context, title: String, quote: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_RECALL)
            .setSmallIcon(android.R.drawable.ic_menu_edit) 
            .setContentTitle("A memory from $title")
            .setContentText("\"$quote\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFF58A6FF.toInt())

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showGuidanceNotification(context: Context, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_GUIDANCE)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Imladris")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(0xFF58A6FF.toInt())

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(42, builder.build())
    }
}
