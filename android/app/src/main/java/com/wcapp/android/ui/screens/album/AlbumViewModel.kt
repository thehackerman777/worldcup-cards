package com.wcapp.android.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.AlbumResponse
import com.wcapp.android.data.remote.UserCardResponse
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
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

    private val _uiState = mutableStateOf(AlbumUiState())
    val uiState: State<AlbumUiState> = _uiState

    init { loadAlbum(); loadRepeated() }

    fun loadAlbum() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val album = apiService.getAlbum()
                _uiState.value = _uiState.value.copy(album = album, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadRepeated() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(repeatedCards = apiService.getRepeatedCards())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun selectTab(tab: Int) { _uiState.value = _uiState.value.copy(selectedTab = tab) }
}
