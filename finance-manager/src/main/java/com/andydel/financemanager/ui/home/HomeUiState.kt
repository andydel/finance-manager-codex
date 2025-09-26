package com.andydel.financemanager.ui.home

import com.andydel.financemanager.domain.model.Account

data class HomeUiState(
    val currentAccounts: List<Account> = emptyList(),
    val savingsAccounts: List<Account> = emptyList(),
    val debtAccounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)
