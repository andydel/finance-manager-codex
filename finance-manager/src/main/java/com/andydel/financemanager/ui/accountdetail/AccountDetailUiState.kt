package com.andydel.financemanager.ui.accountdetail

import com.andydel.financemanager.domain.model.AccountType
import java.time.Instant

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val accountName: String = "",
    val currencySymbol: String = "",
    val currentBalance: Double = 0.0,
    val searchQuery: String = "",
    val transactions: List<AccountTransactionItem> = emptyList(),
    val accountMissing: Boolean = false,
    val accountType: AccountType? = null
) {
    val hasTransactions: Boolean get() = transactions.isNotEmpty()
}

data class AccountTransactionItem(
    val id: Long,
    val timestamp: Instant,
    val description: String,
    val amountChange: Double,
    val isIncome: Boolean,
    val runningBalance: Double
)
