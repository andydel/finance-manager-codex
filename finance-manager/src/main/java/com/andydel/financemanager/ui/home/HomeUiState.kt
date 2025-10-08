package com.andydel.financemanager.ui.home

import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.Currency

data class HomeUiState(
    val currentAccounts: List<Account> = emptyList(),
    val savingsAccounts: List<Account> = emptyList(),
    val debtAccounts: List<Account> = emptyList(),
    val baseCurrency: Currency? = null,
    val baseCurrencyAmounts: Map<Long, Double> = emptyMap(),
    val isLoading: Boolean = true
)
