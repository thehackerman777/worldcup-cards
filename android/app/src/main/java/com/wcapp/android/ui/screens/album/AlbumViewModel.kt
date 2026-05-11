package com.wcapp.android.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.AlbumResponse
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.UserCardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlbumUiState(
    val isLoading: Boolean = false,
    val album: AlbumResponse? = null,
    val repeatedCards: List<UserCardResponse> = emptyList(),
    val selectedTab: Int = 0,
    val error: String? = null
)

class AlbumViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
        loadRepeated()
    }

    fun loadAlbum() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiService.getAlbum().onSuccess { album ->
                _uiState.value = _uiState.value.copy(album = album, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadRepeated() {
        viewModelScope.launch {
            apiService.getRepeatedCards().onSuccess { cards ->
                _uiState.value = _uiState.value.copy(repeatedCards = cards)
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}
