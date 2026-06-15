package com.exapps.omnifetch.ui.state

import com.exapps.omnifetch.domain.model.FormatOption
import com.exapps.omnifetch.domain.model.VideoInfo

data class HomeUiState(
    val isLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val error: String? = null,
    val sharedUrl: String? = null,
    val showFormatSheet: Boolean = false,
    val urlInput: String = "",
    val selectedFormat: FormatOption? = null,
    val isDownloading: Boolean = false
)
