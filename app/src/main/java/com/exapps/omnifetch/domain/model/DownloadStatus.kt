package com.exapps.omnifetch.domain.model

sealed class DownloadStatus {
    data object Idle : DownloadStatus()
    data object Fetching : DownloadStatus()
    data object Queued : DownloadStatus()
    data class Downloading(
        val progress: Float = 0f,
        val speed: String = "",
        val eta: String = ""
    ) : DownloadStatus()
    data object Merging : DownloadStatus()
    data class Completed(val filePath: String = "") : DownloadStatus()
    data class Failed(val error: String = "") : DownloadStatus()
    data object Paused : DownloadStatus()

    fun toSimpleString(): String = when (this) {
        is Idle -> "idle"
        is Fetching -> "fetching"
        is Queued -> "queued"
        is Downloading -> "downloading"
        is Merging -> "merging"
        is Completed -> "completed"
        is Failed -> "failed"
        is Paused -> "paused"
    }

    companion object {
        fun fromString(status: String, progress: Float = 0f, speed: String = "", eta: String = "", error: String = "", filePath: String = ""): DownloadStatus {
            return when (status) {
                "idle" -> Idle
                "fetching" -> Fetching
                "queued" -> Queued
                "downloading" -> Downloading(progress, speed, eta)
                "merging" -> Merging
                "completed" -> Completed(filePath)
                "failed" -> Failed(error)
                "paused" -> Paused
                else -> Idle
            }
        }
    }
}
