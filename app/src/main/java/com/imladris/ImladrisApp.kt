package com.imladris

import android.app.Application
import com.imladris.core.notifications.ImladrisNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ImladrisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ImladrisNotifications.createChannels(this)
    }
}
