package com.exapps.omnifetch.worker

import android.content.Context
import android.os.Environment
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.exapps.omnifetch.data.remote.YtDlpDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSource: YtDlpDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(EXTRA_URL) ?: return Result.failure()
        val formatId = inputData.getString(EXTRA_FORMAT_ID) ?: return Result.failure()
        val title = inputData.getString(EXTRA_TITLE) ?: "Unknown"

        return try {
            val downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "OmniFetch"
            )
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val ext = if (formatId == "audio_only") "mp3" else "mp4"
            val safeName = title.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_").take(100)
            val outputFile = File(downloadDir, "$safeName.$ext")

            dataSource.downloadVideo(url, formatId, outputFile) { progress, etaInSeconds ->
                setProgressAsync(Data.Builder().putFloat("progress", progress).build())
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FORMAT_ID = "extra_format_id"
        const val EXTRA_TITLE = "extra_title"
    }
}
