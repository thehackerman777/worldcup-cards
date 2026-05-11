package com.wcapp.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.wcapp.android.data.local.SessionManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State

data class SettingsUiState(
    val serverUrl: String = "",
    val username: String = "",
    val email: String = "",
    val isDarkMode: Boolean? = null
)

class SettingsViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf(SettingsUiState())
    val uiState: State<SettingsUiState> = _uiState

    init {
        val session = sessionManager.sessionState.value
        _uiState.value = SettingsUiState(
            serverUrl = sessionManager.getServerUrl(),
            username = session.user?.username ?: "",
            email = session.user?.email ?: "",
            isDarkMode = sessionManager.isDarkMode()
        )
    }

    fun updateServerUrl(url: String) {
        sessionManager.updateServerUrl(url)
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }

    fun toggleDarkMode(enabled: Boolean) {
        sessionManager.setDarkMode(enabled)
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }
}
