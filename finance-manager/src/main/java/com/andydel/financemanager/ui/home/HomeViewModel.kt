package com.andydel.financemanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.AccountType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val uiState = repository.observeAccounts()
        .map { accounts ->
            HomeUiState(
                currentAccounts = accounts.filter { it.type == AccountType.CURRENT }.sortedBy { it.sortOrder },
                savingsAccounts = accounts.filter { it.type == AccountType.SAVINGS }.sortedBy { it.sortOrder },
                debtAccounts = accounts.filter { it.type == AccountType.DEBT }.sortedBy { it.sortOrder },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun reorderAccounts(type: AccountType, orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderAccounts(type, orderedIds)
        }
    }

    fun deleteAccounts(accountIds: List<Long>) {
        if (accountIds.isEmpty()) return
        viewModelScope.launch {
            accountIds.forEach { repository.deleteAccount(it) }
        }
    }

    fun applyAccountManagement(change: AccountManagementChange) {
        viewModelScope.launch {
            change.reorderedAccountIdsByType.forEach { (type, ids) ->
                repository.reorderAccounts(type, ids)
            }
            if (change.deletedAccountIds.isNotEmpty()) {
                change.deletedAccountIds.forEach { repository.deleteAccount(it) }
            }
        }
    }
}
