package com.andydel.financemanager.ui.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountDetailViewModel(
    private val accountId: Long,
    private val repository: FinanceRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState = combine(
        repository.observeAccounts(),
        repository.observeTransactions(accountId),
        searchQuery
    ) { accounts, transactions, query ->
        val account = accounts.firstOrNull { it.id == accountId }
            ?: return@combine AccountDetailUiState(
                isLoading = false,
                searchQuery = query,
                accountMissing = true
            )

        var runningBalance = account.initialBalance
        val computed = transactions
            .sortedBy { it.timestamp }
            .map { transaction ->
                runningBalance += if (transaction.type == TransactionType.INCOME) {
                    transaction.amount
                } else {
                    -transaction.amount
                }
                AccountTransactionItem(
                    id = transaction.id,
                    timestamp = transaction.timestamp,
                    description = transaction.description,
                    amount = transaction.amount,
                    isIncome = transaction.type == TransactionType.INCOME,
                    runningBalance = runningBalance
                )
            }
            .sortedByDescending { it.timestamp }

        val filtered = if (query.isBlank()) computed else computed.filter {
            it.description.contains(query, ignoreCase = true)
        }

        AccountDetailUiState(
            isLoading = false,
            accountName = account.name,
            currencySymbol = account.currency.symbol,
            currentBalance = account.currentBalance,
            searchQuery = query,
            transactions = filtered
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountDetailUiState()
        )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    companion object {
        fun provideFactory(
            repository: FinanceRepository,
            accountId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AccountDetailViewModel::class.java)) {
                    return AccountDetailViewModel(accountId, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class ${'$'}{modelClass.name}")
            }
        }
    }
}
