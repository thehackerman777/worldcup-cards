package com.wcapp.android.ui.screens.panini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
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
    val nickname: String,
    val duplicates: List<String>,
    val missing: List<String>,
    val completion: Int,
    val lastSync: String,
    val profileFound: Boolean,
    val fromCache: Boolean,
    val source: PaniniSource
)

data class PaniniSearchResult(
    val nickname: String,
    val displayName: String?,
    val completion: Int,
    val duplicateCount: Int
)

class PaniniViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = mutableStateOf(PaniniUiState())
    val uiState: State<PaniniUiState> = _uiState

    fun lookupUser(nickname: String, source: PaniniSource = PaniniSource.LOCAL) {
        if (nickname.isBlank()) return

        viewModelScope.launch {
            _uiState.value = PaniniUiState(
                isLoading = true,
                nickname = nickname,
                source = source
            )

            val result = when (source) {
                PaniniSource.LOCAL -> apiService.paniniLocalLookup(nickname.trim())
                PaniniSource.EXTERNAL -> apiService.paniniExternalLookup(nickname.trim())
            }

            when (result) {
                is com.wcapp.android.data.remote.ApiResult.Success -> {
                    val response = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userData = PaniniUserData(
                            nickname = response.nickname,
                            duplicates = response.duplicates,
                            missing = response.missing,
                            completion = response.completion,
                            lastSync = response.lastSync,
                            profileFound = response.profileFound,
                            fromCache = response.fromCache,
                            source = source
                        )
                    )
                }
                is com.wcapp.android.data.remote.ApiResult.Error -> {
                    val msg = result.message ?: "Error de conexión"
                    val userMsg = when {
                        msg.contains("404") || msg.contains("not found", ignoreCase = true) ->
                            "Usuario '$nickname' no encontrado en ${source.name.lowercase()}. " +
                            if (source == PaniniSource.LOCAL) "Sincroniza primero desde la app."
                            else "Verifica que el nickname exista en Panini."
                        msg.contains("410") || msg.contains("expir", ignoreCase = true) ->
                            "Datos de '$nickname' expirados. Sincroniza de nuevo."
                        msg.contains("503") || msg.contains("unavailable", ignoreCase = true) ||
                        msg.contains("no se pudo conectar", ignoreCase = true) ->
                            "Base de datos externa de Panini no disponible. " +
                            "Verifica que la API esté configurada en el servidor."
                        else -> msg
                    }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = userMsg
                )
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = apiService.paniniSearch(query.trim())) {
                is com.wcapp.android.data.remote.ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = result.data.results.map {
                            PaniniSearchResult(
                                nickname = it.nickname,
                                displayName = it.displayName,
                                completion = it.completion,
                                duplicateCount = it.duplicateCount
                            )
                        }
                    )
                }
                is com.wcapp.android.data.remote.ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearSearch() {
        _uiState.value = PaniniUiState()
    }
}
