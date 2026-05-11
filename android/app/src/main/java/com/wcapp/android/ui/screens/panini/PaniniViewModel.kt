package com.wcapp.android.ui.screens.panini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.launch

enum class PaniniSource { LOCAL, EXTERNAL }

data class PaniniUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val source: PaniniSource = PaniniSource.LOCAL,
    val userData: PaniniUserData? = null,
    val searchResults: List<PaniniSearchResult> = emptyList(),
    val error: String? = null
)

data class PaniniUserData(
    val nickname: String, val duplicates: List<String>, val missing: List<String>,
    val completion: Int, val lastSync: String, val profileFound: Boolean,
    val fromCache: Boolean, val source: PaniniSource
)

data class PaniniSearchResult(
    val nickname: String, val displayName: String?, val completion: Int, val duplicateCount: Int
)

class PaniniViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = mutableStateOf(PaniniUiState())
    val uiState: State<PaniniUiState> = _uiState

    fun lookupUser(nickname: String, source: PaniniSource = PaniniSource.LOCAL) {
        if (nickname.isBlank()) return
        viewModelScope.launch {
            _uiState.value = PaniniUiState(isLoading = true, nickname = nickname, source = source)
            try {
                val response = when (source) { PaniniSource.LOCAL -> apiService.paniniLocalLookup(nickname.trim()); PaniniSource.EXTERNAL -> apiService.paniniExternalLookup(nickname.trim()) }
                _uiState.value = _uiState.value.copy(isLoading = false, userData = PaniniUserData(nickname = response.nickname, duplicates = response.duplicates, missing = response.missing, completion = response.completion, lastSync = response.lastSync, profileFound = response.profileFound, fromCache = response.fromCache, source = source))
            } catch (e: Exception) {
                val msg = e.message ?: "Error de conexión"
                _uiState.value = _uiState.value.copy(isLoading = false, error = when { msg.contains("404") || msg.contains("not found", true) -> "Usuario no encontrado en ${source.name.lowercase()}"
                    msg.contains("410") || msg.contains("expir", true) -> "Datos expirados. Sincroniza de nuevo."
                    else -> msg })
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.paniniSearch(query.trim())
                _uiState.value = _uiState.value.copy(isLoading = false, searchResults = response.results.map { PaniniSearchResult(nickname = it.nickname, displayName = it.displayName, completion = it.completion, duplicateCount = it.duplicateCount) })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error en búsqueda")
            }
        }
    }

    fun clearSearch() { _uiState.value = PaniniUiState() }
}
