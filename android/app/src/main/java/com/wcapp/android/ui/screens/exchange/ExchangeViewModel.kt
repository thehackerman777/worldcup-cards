package com.wcapp.android.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.launch

data class ExchangeUiState(
    val isLoading: Boolean = false,
    val myExchanges: List<ExchangeResponse> = emptyList(),
    val availableExchanges: List<ExchangeResponse> = emptyList(),
    val selectedTab: Int = 0,
    val error: String? = null,
    val actionSuccess: String? = null
)

class ExchangeViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = mutableStateOf(ExchangeUiState())
    val uiState: State<ExchangeUiState> = _uiState

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val exchangeResult = apiService.getExchanges()
            when (exchangeResult) {
                is com.wcapp.android.data.remote.ApiResult.Success -> {
                    val response = exchangeResult.data
                _uiState.value = _uiState.value.copy(myExchanges = response.exchanges)
            }

            apiService.getAvailableExchanges().onSuccess {
                _uiState.value = _uiState.value.copy(availableExchanges = response.exchanges)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun createExchange(request: CreateExchangeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            apiService.createExchange(request).onSuccess {
                _uiState.value = _uiState.value.copy(
                    actionSuccess = "Intercambio creado con éxito",
                    isLoading = false
                )
                loadAll()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun acceptExchange(id: String) {
        viewModelScope.launch {
            apiService.acceptExchange(id).onSuccess {
                _uiState.value = _uiState.value.copy(actionSuccess = "Intercambio aceptado")
                loadAll()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun rejectExchange(id: String) {
        viewModelScope.launch {
            apiService.rejectExchange(id).onSuccess {
                _uiState.value = _uiState.value.copy(actionSuccess = "Intercambio rechazado")
                loadAll()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun completeExchange(id: String) {
        viewModelScope.launch {
            apiService.completeExchange(id).onSuccess {
                _uiState.value = _uiState.value.copy(actionSuccess = "Intercambio completado")
                loadAll()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, actionSuccess = null)
    }
}
