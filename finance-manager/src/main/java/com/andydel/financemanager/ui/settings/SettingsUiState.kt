package com.andydel.financemanager.ui.settings

import com.andydel.financemanager.domain.model.Currency

data class SettingsUiState(
    val name: String = "",
    val currencies: List<Currency> = emptyList(),
    val selectedCurrencyId: Long? = null,
    val exchangeRateApiKey: String = "",
    val isSaving: Boolean = false,
    val isInitialized: Boolean = false,
    val message: String? = null
)
