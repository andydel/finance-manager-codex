package com.andydel.financemanager.ui.addaccount

import com.andydel.financemanager.domain.model.AccountType
import com.andydel.financemanager.domain.model.Currency

data class AddAccountUiState(
    val name: String = "",
    val initialBalance: String = "",
    val selectedType: AccountType = AccountType.CURRENT,
    val availableCurrencies: List<Currency> = emptyList(),
    val selectedCurrencyId: Long? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = name.isNotBlank() && selectedCurrencyId != null
}
