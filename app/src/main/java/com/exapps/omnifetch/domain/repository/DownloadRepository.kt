package com.exapps.omnifetch.domain.repository

import com.exapps.omnifetch.domain.model.DownloadItem
import com.exapps.omnifetch.domain.model.DownloadStatus
import com.exapps.omnifetch.domain.model.VideoInfo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun fetchVideoInfo(url: String): Result<VideoInfo>
    fun startDownload(
        url: String,
        formatId: String,
        title: String,
        thumbnail: String
    ): Flow<DownloadStatus>
    fun getDownloadHistory(): Flow<List<DownloadItem>>
    fun updateYtDlp(): Flow<Result<Unit>>
    suspend fun deleteDownload(id: Long)
    suspend fun insertDownload(item: DownloadItem): Long
    suspend fun updateDownloadStatus(id: Long, status: DownloadStatus)
    suspend fun updateDownloadProgress(id: Long, progress: Float, speed: String, eta: String)
}
