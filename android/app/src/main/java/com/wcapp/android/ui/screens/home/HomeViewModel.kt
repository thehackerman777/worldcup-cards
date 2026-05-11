package com.wcapp.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.local.SessionManager
import com.wcapp.android.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val albumCompletion: Double = 0.0,
    val repeatedCount: Int = 0,
    val pendingExchanges: Int = 0,
    val error: String? = null
)

class HomeViewModel(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val username = sessionManager.sessionState.value.user?.displayName
                ?: sessionManager.sessionState.value.user?.username
                ?: ""

            _uiState.value = _uiState.value.copy(username = username)

            // Load album stats
            apiService.getAlbum().onSuccess { album ->
                _uiState.value = _uiState.value.copy(
                    albumCompletion = album.completionPercentage,
                    repeatedCount = album.repeatedCards
                )
            }

            // Load pending exchanges
            apiService.getExchanges().onSuccess { exchanges ->
                val pending = exchanges.exchanges.count { it.status == "PENDING" }
                _uiState.value = _uiState.value.copy(pendingExchanges = pending)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
