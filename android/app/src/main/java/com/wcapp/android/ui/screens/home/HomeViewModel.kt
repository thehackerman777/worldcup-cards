package com.wcapp.android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.local.SessionManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
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
    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val username = sessionManager.sessionState.value.user?.displayName ?: sessionManager.sessionState.value.user?.username ?: ""
            _uiState.value = _uiState.value.copy(username = username)
            try { val album = apiService.getAlbum(); _uiState.value = _uiState.value.copy(albumCompletion = album.completionPercentage, repeatedCount = album.repeatedCards) } catch (_: Exception) {}
            try { val ex = apiService.getExchanges(); _uiState.value = _uiState.value.copy(pendingExchanges = ex.exchanges.count { it.status == "PENDING" }) } catch (_: Exception) {}
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() { sessionManager.clearSession() }
}
