package com.andydel.financemanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        observeCurrencies()
        observeUser()
    }

    private fun observeCurrencies() {
        viewModelScope.launch {
            repository.observeCurrencies().collectLatest { currencies ->
                _uiState.value = _uiState.value.copy(
                    currencies = currencies,
                    selectedCurrencyId = _uiState.value.selectedCurrencyId ?: currencies.firstOrNull()?.id
                )
            }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            repository.observeUserProfile().collectLatest { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        name = profile.name,
                        selectedCurrencyId = profile.baseCurrency.id,
                        exchangeRateApiKey = profile.exchangeRateApiKey.orEmpty(),
                        isInitialized = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isInitialized = true)
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onCurrencySelected(currencyId: Long) {
        _uiState.value = _uiState.value.copy(selectedCurrencyId = currencyId)
    }

    fun onExchangeRateApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(exchangeRateApiKey = value)
    }

    fun saveSettings() {
        val state = _uiState.value
        val currencyId = state.selectedCurrencyId ?: return
        if (state.name.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, message = null)
                repository.upsertUser(state.name.trim(), currencyId, state.exchangeRateApiKey)
                _uiState.value = _uiState.value.copy(isSaving = false, message = "Settings saved")
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = t.localizedMessage ?: "Unable to save settings"
                )
            }
        }
    }
}
