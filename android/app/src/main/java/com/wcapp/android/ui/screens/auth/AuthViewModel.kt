package com.wcapp.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.local.SessionManager
import com.wcapp.android.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = apiService.login(username, password)

            result.onSuccess { response ->
                if (response.token.isNotBlank() && response.user != null) {
                    sessionManager.saveSession(
                        token = response.token,
                        refreshToken = response.refreshToken,
                        userId = response.user.id,
                        username = response.user.username,
                        email = response.user.email,
                        displayName = response.user.displayName
                    )
                    _uiState.value = AuthUiState(isSuccess = true)
                } else {
                    _uiState.value = AuthUiState(error = "Error al iniciar sesión")
                }
            }.onFailure { e ->
                _uiState.value = AuthUiState(error = e.message ?: "Error de conexión")
            }
        }
    }

    fun register(username: String, email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = apiService.register(username, email, password, displayName)

            result.onSuccess { response ->
                if (response.token.isNotBlank() && response.user != null) {
                    sessionManager.saveSession(
                        token = response.token,
                        refreshToken = response.refreshToken,
                        userId = response.user.id,
                        username = response.user.username,
                        email = response.user.email,
                        displayName = response.user.displayName
                    )
                    _uiState.value = AuthUiState(isSuccess = true)
                } else {
                    _uiState.value = AuthUiState(error = "Error al registrarse")
                }
            }.onFailure { e ->
                _uiState.value = AuthUiState(error = e.message ?: "Error de conexión")
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState()
    }
}
