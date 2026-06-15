package com.exapps.omnifetch.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exapps.omnifetch.domain.usecase.UpdateYtDlpUseCase
import com.exapps.omnifetch.ui.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateYtDlpUseCase: UpdateYtDlpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onUpdateYtDlp() {
        _uiState.update { it.copy(isUpdating = true, updateResult = null) }

        viewModelScope.launch {
            updateYtDlpUseCase().collect { result ->
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateResult = result
                    )
                }
            }
        }
    }

    fun onToggleAutoUpdate() {
        _uiState.update { it.copy(autoUpdate = !it.autoUpdate) }
    }

    fun onDismissUpdateResult() {
        _uiState.update { it.copy(updateResult = null) }
    }
}
