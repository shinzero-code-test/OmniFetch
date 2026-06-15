package com.exapps.omnifetch.domain.model

data class DownloadItem(
    val id: Long = 0L,
    val title: String = "",
    val thumbnail: String = "",
    val url: String = "",
    val status: DownloadStatus = DownloadStatus.Idle,
    val progress: Float = 0f,
    val filePath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val fileSize: Long? = null,
    val formatId: String = "",
    val extension: String = "",
    val speed: String = "",
    val eta: String = ""
)
