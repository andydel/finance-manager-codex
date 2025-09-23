package com.andydel.financemanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.AccountType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: FinanceRepository
) : ViewModel() {

    val uiState = repository.observeAccounts()
        .map { accounts ->
            HomeUiState(
                currentAccounts = accounts.filter { it.type == AccountType.CURRENT },
                savingsAccounts = accounts.filter { it.type == AccountType.SAVINGS },
                debtAccounts = accounts.filter { it.type == AccountType.DEBT },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}
