package com.exapps.omnifetch.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.exapps.omnifetch.MainActivity
import com.exapps.omnifetch.OmniFetchApp
import com.exapps.omnifetch.data.remote.YtDlpDataSource
import com.exapps.omnifetch.worker.DownloadProgress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject lateinit var dataSource: YtDlpDataSource

    private val binder = DownloadBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentDownloadJob: Job? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification("OmniFetch", "Ready to download"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val formatId = intent.getStringExtra(EXTRA_FORMAT_ID) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown"
                val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0L)
                startDownload(url, formatId, title, downloadId)
            }
            ACTION_CANCEL -> {
                cancelCurrentDownload()
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownload(url: String, formatId: String, title: String, downloadId: Long) {
        currentDownloadJob?.cancel()
        currentDownloadJob = serviceScope.launch {
            try {
                updateNotification(title, "Downloading...")

                val downloadDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "OmniFetch"
                )
                if (!downloadDir.exists()) downloadDir.mkdirs()

                val ext = if (formatId == "audio_only") "mp3" else "mp4"
                val safeName = title.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_").take(100)
                val outputFile = File(downloadDir, "$safeName.$ext")

                var lastProgress = 0f

                dataSource.downloadVideo(url, formatId, outputFile) { progress, etaInSeconds ->
                    if (progress - lastProgress >= 1f || progress >= 100f) {
                        lastProgress = progress
                        val eta = if (etaInSeconds > 0) "${etaInSeconds}s" else ""
                        updateNotification(title, "%.0f%% - ETA: $eta".format(progress))
                        _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                            put(downloadId, DownloadProgress(
                                downloadId = downloadId,
                                progress = progress,
                                speed = "",
                                eta = eta
                            ))
                        }
                    }
                }

                _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                    put(downloadId, DownloadProgress(downloadId = downloadId, progress = 100f))
                }
                updateNotification(title, "Download complete")
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                updateNotification(title, "Download failed: ${e.message}")
            }
        }
    }

    private fun cancelCurrentDownload() {
        currentDownloadJob?.cancel()
        currentDownloadJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OmniFetchApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(title, content))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }

    companion object {
        private const val TAG = "DownloadService"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.exapps.omnifetch.DOWNLOAD_START"
        const val ACTION_CANCEL = "com.exapps.omnifetch.DOWNLOAD_CANCEL"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FORMAT_ID = "extra_format_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
        val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress

        fun startDownloadService(
            context: Context,
            url: String,
            formatId: String,
            title: String,
            downloadId: Long
        ) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_FORMAT_ID, formatId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
