package com.exapps.omnifetch.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.exapps.omnifetch.data.local.dao.DownloadDao
import com.exapps.omnifetch.data.local.entity.DownloadEntity
import com.exapps.omnifetch.data.remote.YtDlpDataSource
import com.exapps.omnifetch.domain.model.DownloadItem
import com.exapps.omnifetch.domain.model.DownloadStatus
import com.exapps.omnifetch.domain.model.VideoInfo
import com.exapps.omnifetch.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val context: Context,
    private val dataSource: YtDlpDataSource,
    private val downloadDao: DownloadDao
) : DownloadRepository {

    override suspend fun fetchVideoInfo(url: String): Result<VideoInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val info = if (isPlaylistUrl(url)) {
                    dataSource.fetchPlaylistInfo(url)
                } else {
                    dataSource.fetchVideoInfo(url)
                }
                Result.success(info)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch video info", e)
                Result.failure(e)
            }
        }
    }

    override fun startDownload(
        url: String,
        formatId: String,
        title: String,
        thumbnail: String
    ): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Fetching)

        val entity = DownloadEntity(
            title = title,
            thumbnail = thumbnail,
            url = url,
            status = "downloading",
            formatId = formatId,
            extension = if (formatId == "audio_only") "mp3" else "mp4"
        )
        val id = downloadDao.insert(entity)
        emit(DownloadStatus.Downloading(0f, "", ""))

        try {
            val downloadDir = getDownloadDirectory()
            val ext = if (formatId == "audio_only") "mp3" else "mp4"
            val safeName = sanitizeFileName(title)
            val outputFile = File(downloadDir, "$safeName.$ext")

            var lastProgress = 0f

            withContext(Dispatchers.IO) {
                dataSource.downloadVideo(url, formatId, outputFile) { progress, etaInSeconds, _ ->
                    if (progress - lastProgress >= 1f || progress >= 100f) {
                        lastProgress = progress
                        val eta = if (etaInSeconds > 0) "${etaInSeconds}s" else ""
                        try {
                            kotlinx.coroutines.runBlocking {
                                downloadDao.updateProgress(id, progress, "", eta)
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            val finalFile = outputFile
            if (finalFile.exists() && finalFile.length() > 0) {
                downloadDao.updateCompleted(
                    id = id,
                    status = "completed",
                    progress = 100f,
                    filePath = finalFile.absolutePath,
                    completedAt = System.currentTimeMillis()
                )
                emit(DownloadStatus.Completed(finalFile.absolutePath))
            } else {
                downloadDao.updateStatus(id, "failed")
                emit(DownloadStatus.Failed("Downloaded file not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            downloadDao.updateStatus(id, "failed")
            emit(DownloadStatus.Failed(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getDownloadHistory(): Flow<List<DownloadItem>> {
        return flow {
            downloadDao.getAll().collect { entities ->
                emit(entities.map { it.toDomain() })
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun updateYtDlp(): Flow<Result<Unit>> = flow {
        try {
            withContext(Dispatchers.IO) {
                dataSource.updateBinary()
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update yt-dlp", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteDownload(id: Long) {
        withContext(Dispatchers.IO) {
            val entity = downloadDao.getById(id)
            if (entity != null && entity.filePath.isNotEmpty()) {
                try {
                    File(entity.filePath).delete()
                } catch (_: Exception) {}
            }
            downloadDao.deleteById(id)
        }
    }

    override suspend fun insertDownload(item: DownloadItem): Long {
        return downloadDao.insert(DownloadEntity.fromDomain(item))
    }

    override suspend fun updateDownloadStatus(id: Long, status: DownloadStatus) {
        downloadDao.updateStatus(id, status.toSimpleString())
    }

    override suspend fun updateDownloadProgress(id: Long, progress: Float, speed: String, eta: String) {
        downloadDao.updateProgress(id, progress, speed, eta)
    }

    private fun getDownloadDirectory(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "OmniFetch"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            .replace(Regex("\\s+"), "_")
            .take(100)
    }

    private fun isPlaylistUrl(url: String): Boolean {
        return url.contains("list=") ||
                url.contains("/playlist") ||
                url.contains("playlist?")
    }

    companion object {
        private const val TAG = "DownloadRepository"
    }
}
