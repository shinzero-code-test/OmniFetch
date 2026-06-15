package com.exapps.omnifetch.ui.screens.home

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.omnifetch.domain.model.DownloadStatus
import com.exapps.omnifetch.domain.model.FormatOption
import com.exapps.omnifetch.domain.usecase.GetVideoMetadataUseCase
import com.exapps.omnifetch.domain.usecase.StartDownloadUseCase
import com.exapps.omnifetch.ui.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getVideoMetadataUseCase: GetVideoMetadataUseCase,
    private val startDownloadUseCase: StartDownloadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(urlInput = url, error = null) }
    }

    fun onSubmitUrl() {
        val url = _uiState.value.urlInput.trim()
        if (url.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a URL") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, videoInfo = null) }

        viewModelScope.launch {
            getVideoMetadataUseCase(url).collect { result ->
                result.fold(
                    onSuccess = { info ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                videoInfo = info,
                                showFormatSheet = true
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to fetch video info"
                            )
                        }
                    }
                )
            }
        }
    }

    fun onFormatSelected(format: FormatOption) {
        val videoInfo = _uiState.value.videoInfo ?: return

        _uiState.update {
            it.copy(
                showFormatSheet = false,
                isDownloading = true,
                selectedFormat = format
            )
        }

        viewModelScope.launch {
            startDownloadUseCase(
                url = videoInfo.webpageUrl,
                formatId = format.formatId,
                title = videoInfo.title,
                thumbnail = videoInfo.thumbnail
            ).collect { status ->
                when (status) {
                    is DownloadStatus.Completed -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                error = null
                            )
                        }
                    }
                    is DownloadStatus.Failed -> {
                        _uiState.update {
                            it.copy(
                                isDownloading = false,
                                error = status.error
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onDismissFormatSheet() {
        _uiState.update { it.copy(showFormatSheet = false) }
    }

    fun onSharedUrlReceived(url: String) {
        _uiState.update { it.copy(sharedUrl = url, urlInput = url) }
        onSubmitUrl()
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onDismissSuccess() {
        _uiState.update { it.copy(isDownloading = false) }
    }
}
