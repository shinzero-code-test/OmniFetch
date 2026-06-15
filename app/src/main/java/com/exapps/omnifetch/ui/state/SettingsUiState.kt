package com.exapps.omnifetch.ui.state

data class SettingsUiState(
    val isUpdating: Boolean = false,
    val updateResult: Result<Unit>? = null,
    val autoUpdate: Boolean = false,
    val appVersion: String = "1.0.0"
)
