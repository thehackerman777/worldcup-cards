package com.wcapp.android.ui.screens.panini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaniniUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
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
    val fromCache: Boolean
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

    private val _uiState = MutableStateFlow(PaniniUiState())
    val uiState: StateFlow<PaniniUiState> = _uiState.asStateFlow()

    fun lookupUser(nickname: String) {
        if (nickname.isBlank()) return

        viewModelScope.launch {
            _uiState.value = PaniniUiState(isLoading = true, nickname = nickname)

            apiService.paniniLookup(nickname.trim()).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userData = PaniniUserData(
                        nickname = response.nickname,
                        duplicates = response.duplicates,
                        missing = response.missing,
                        completion = response.completion,
                        lastSync = response.lastSync,
                        profileFound = response.profileFound,
                        fromCache = response.fromCache
                    )
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al consultar usuario Panini"
                )
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            apiService.paniniSearch(query.trim()).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = response.results.map {
                        PaniniSearchResult(
                            nickname = it.nickname,
                            displayName = it.displayName,
                            completion = it.completion,
                            duplicateCount = it.duplicateCount
                        )
                    }
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = PaniniUiState()
    }
}
