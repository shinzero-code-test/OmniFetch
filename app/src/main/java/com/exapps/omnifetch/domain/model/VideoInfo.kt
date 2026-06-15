package com.exapps.omnifetch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
    val id: String = "",
    val title: String = "",
    val thumbnail: String = "",
    val duration: Long = 0L,
    val uploader: String = "",
    val webpageUrl: String = "",
    val formats: List<FormatOption> = emptyList(),
    val isPlaylist: Boolean = false,
    val playlistItems: List<VideoInfo> = emptyList()
)
