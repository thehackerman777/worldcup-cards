package com.wcapp.android.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.android.data.remote.*
import androidx.compose.runtime.mutableStateOf
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

class ExchangeViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = mutableStateOf(ExchangeUiState())
    val uiState: State<ExchangeUiState> = _uiState

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try { _uiState.value = _uiState.value.copy(myExchanges = apiService.getExchanges().exchanges) } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
            try { _uiState.value = _uiState.value.copy(availableExchanges = apiService.getAvailableExchanges().exchanges) } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun createExchange(request: CreateExchangeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try { apiService.createExchange(request); _uiState.value = _uiState.value.copy(actionSuccess = "Intercambio creado"); loadAll() }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
        }
    }

    fun acceptExchange(id: String) { viewModelScope.launch { try { apiService.acceptExchange(id); _uiState.value = _uiState.value.copy(actionSuccess = "Aceptado"); loadAll() } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) } } }
    fun rejectExchange(id: String) { viewModelScope.launch { try { apiService.rejectExchange(id); _uiState.value = _uiState.value.copy(actionSuccess = "Rechazado"); loadAll() } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) } } }
    fun completeExchange(id: String) { viewModelScope.launch { try { apiService.completeExchange(id); _uiState.value = _uiState.value.copy(actionSuccess = "Completado"); loadAll() } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) } } }
    fun selectTab(tab: Int) { _uiState.value = _uiState.value.copy(selectedTab = tab) }
    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, actionSuccess = null) }
}
