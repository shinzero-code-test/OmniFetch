package com.exapps.omnifetch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FormatOption(
    val formatId: String = "",
    val extension: String = "",
    val resolution: String = "",
    val fileSize: Long? = null,
    val vcodec: String = "none",
    val acodec: String = "none",
    val fps: Int? = null,
    val isAudioOnly: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    val tbr: Double? = null
)
