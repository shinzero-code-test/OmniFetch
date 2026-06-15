package com.exapps.omnifetch.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.omnifetch.domain.model.DownloadItem
import com.exapps.omnifetch.domain.repository.DownloadRepository
import com.exapps.omnifetch.ui.state.DownloadFilter
import com.exapps.omnifetch.ui.state.DownloadsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    private var allDownloads: List<DownloadItem> = emptyList()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getDownloadHistory().collect { downloads ->
                allDownloads = downloads
                applyFilter()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFilterChanged(filter: DownloadFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_uiState.value.selectedFilter) {
            DownloadFilter.ALL -> allDownloads
            DownloadFilter.DOWNLOADING -> allDownloads.filter {
                it.status.toSimpleString() in listOf("downloading", "fetching", "queued")
            }
            DownloadFilter.COMPLETED -> allDownloads.filter {
                it.status.toSimpleString() == "completed"
            }
            DownloadFilter.FAILED -> allDownloads.filter {
                it.status.toSimpleString() == "failed"
            }
        }
        _uiState.update { it.copy(downloads = filtered) }
    }

    fun onDeleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun onRetryDownload(item: DownloadItem) {
        viewModelScope.launch {
            repository.startDownload(
                url = item.url,
                formatId = item.formatId,
                title = item.title,
                thumbnail = item.thumbnail
            )
        }
    }
}
