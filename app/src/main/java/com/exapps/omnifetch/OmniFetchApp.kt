package com.exapps.omnifetch

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.github.yausername.youtubedl_android.YoutubeDL
import com.github.yausername.youtubedl_android.YoutubeDLException
import com.github.yausername.youtubedl_android.FFmpeg
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OmniFetchApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initYoutubeDL()
        initFFmpeg()
        createNotificationChannels()
    }

    private fun initYoutubeDL() {
        try {
            YoutubeDL.getInstance().init(this)
            Log.d(TAG, "YoutubeDL initialized successfully")
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "Failed to initialize YoutubeDL", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize YoutubeDL", e)
        }
    }

    private fun initFFmpeg() {
        try {
            FFmpeg.getInstance().init(this)
            Log.d(TAG, "FFmpeg initialized successfully")
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "Failed to initialize FFmpeg", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FFmpeg", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Download completion alerts"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(downloadChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val TAG = "OmniFetchApp"
        const val DOWNLOAD_CHANNEL_ID = "omnifetch_downloads"
        const val ALERT_CHANNEL_ID = "omnifetch_alerts"

        lateinit var instance: OmniFetchApp
            private set
    }
}
