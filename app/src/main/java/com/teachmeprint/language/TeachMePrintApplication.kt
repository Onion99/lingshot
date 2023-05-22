package com.teachmeprint.language

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.os.Build
import com.orhanobut.hawk.Hawk
import com.teachmeprint.common.CommonConstant.CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TeachMePrintApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        setupHawk()
        setupTimber()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupHawk() {
        Hawk.init(this).build()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        private const val CHANNEL_NAME = "Teach Me Print"
        private const val CHANNEL_DESCRIPTION = "Language Learning"
    }
}
