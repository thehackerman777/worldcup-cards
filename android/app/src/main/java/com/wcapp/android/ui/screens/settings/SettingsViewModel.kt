package com.wcapp.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.wcapp.android.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val serverUrl: String = "",
    val username: String = "",
    val email: String = "",
    val isDarkMode: Boolean? = null
)

class SettingsViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
