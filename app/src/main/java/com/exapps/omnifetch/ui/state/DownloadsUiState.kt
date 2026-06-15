package com.exapps.omnifetch.ui.state

import com.exapps.omnifetch.domain.model.DownloadItem

data class DownloadsUiState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: DownloadFilter = DownloadFilter.ALL
)

enum class DownloadFilter {
    ALL, DOWNLOADING, COMPLETED, FAILED
}
