package com.wcapp.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.local.SessionManager
import com.wcapp.android.data.remote.ApiResult
import com.wcapp.android.data.remote.ApiService
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val detail: String? = null
)

class AuthViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = mutableStateOf(AuthUiState())
    val uiState: State<AuthUiState> = _uiState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (val result = apiService.login(username, password)) {
                is ApiResult.Success -> {
                    val response = result.data
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
                        _uiState.value = AuthUiState(
                            error = "Respuesta inválida del servidor",
                            detail = "token=${response.token.take(10)}..."
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = AuthUiState(
                        error = result.message,
                        detail = result.detail
                    )
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (val result = apiService.register(username, email, password, displayName)) {
                is ApiResult.Success -> {
                    val response = result.data
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
                        _uiState.value = AuthUiState(
                            error = "Respuesta inválida del servidor",
                            detail = "token=${response.token.take(10)}..."
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = AuthUiState(
                        error = result.message,
                        detail = result.detail
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState()
    }
}
