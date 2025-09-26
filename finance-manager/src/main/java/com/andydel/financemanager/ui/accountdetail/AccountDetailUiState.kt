package com.andydel.financemanager.ui.accountdetail

import java.time.Instant

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val accountName: String = "",
    val currencySymbol: String = "",
    val currentBalance: Double = 0.0,
    val searchQuery: String = "",
    val transactions: List<AccountTransactionItem> = emptyList(),
    val accountMissing: Boolean = false
) {
    val hasTransactions: Boolean get() = transactions.isNotEmpty()
}

data class AccountTransactionItem(
    val id: Long,
    val timestamp: Instant,
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val runningBalance: Double
)
