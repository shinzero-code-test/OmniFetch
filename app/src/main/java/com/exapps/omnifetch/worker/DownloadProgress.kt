package com.exapps.omnifetch.worker

data class DownloadProgress(
    val downloadId: Long = 0L,
    val progress: Float = 0f,
    val speed: String = "",
    val eta: String = "",
    val totalSize: Long = 0L,
    val downloadedSize: Long = 0L
)
