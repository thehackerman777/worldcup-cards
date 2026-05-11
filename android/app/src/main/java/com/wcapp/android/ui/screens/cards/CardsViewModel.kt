package com.wcapp.android.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.data.remote.CardResponse
import com.wcapp.android.data.remote.CardsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardsUiState(
    val isLoading: Boolean = false,
    val cards: List<CardResponse> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val selectedTeam: String? = null,
    val error: String? = null
)

class CardsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards(page: Int = 0, team: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedTeam = team)
            apiService.getCards(page = page, team = team).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    cards = if (page == 0) response.cards else _uiState.value.cards + response.cards,
                    currentPage = response.currentPage,
                    totalPages = response.totalPages,
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.isLoading && state.currentPage < state.totalPages - 1) {
            loadCards(state.currentPage + 1, state.selectedTeam)
        }
    }
}
