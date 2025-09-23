package com.andydel.financemanager.ui.addtransaction

import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.Category
import com.andydel.financemanager.domain.model.TransactionType

data class AddTransactionUiState(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val amount: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = amount.toDoubleOrNull()?.let { it > 0.0 } == true && selectedAccountId != null && selectedCategoryId != null
}
