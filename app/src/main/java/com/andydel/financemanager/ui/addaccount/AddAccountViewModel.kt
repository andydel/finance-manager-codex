package com.andydel.financemanager.ui.addaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddAccountViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState

    init {
        observeCurrencies()
        observeDefaultCurrency()
    }

    private fun observeCurrencies() {
        viewModelScope.launch {
            repository.observeCurrencies().collectLatest { currencies ->
                _uiState.value = _uiState.value.copy(
                    availableCurrencies = currencies,
                    selectedCurrencyId = _uiState.value.selectedCurrencyId ?: currencies.firstOrNull()?.id
                )
            }
        }
    }

    private fun observeDefaultCurrency() {
        viewModelScope.launch {
            repository.observeUserProfile().collectLatest { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(selectedCurrencyId = user.baseCurrency.id)
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onBalanceChanged(balance: String) {
        _uiState.value = _uiState.value.copy(initialBalance = balance)
    }

    fun onTypeSelected(type: AccountType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onCurrencySelected(currencyId: Long) {
        _uiState.value = _uiState.value.copy(selectedCurrencyId = currencyId)
    }

    fun loadAccountForEdit(accountId: Long?) {
        if (accountId == null || accountId <= 0L || _uiState.value.editingAccountId == accountId) {
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            mode = AccountFormMode.EDIT,
            editingAccountId = accountId,
            errorMessage = null
        )
        viewModelScope.launch {
            val account = repository.getAccount(accountId)
            if (account != null) {
                _uiState.value = _uiState.value.copy(
                    name = account.name,
                    initialBalance = account.initialBalance.toString(),
                    selectedType = account.type,
                    selectedCurrencyId = account.currency.id,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Account not found",
                    isLoading = false
                )
            }
        }
    }

    fun saveAccount(defaultUserId: Long? = null) {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        val balanceValue = state.initialBalance.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, errorMessage = null)
                if (state.mode == AccountFormMode.EDIT && state.editingAccountId != null) {
                    repository.updateAccount(
                        accountId = state.editingAccountId,
                        name = state.name.trim(),
                        type = state.selectedType,
                        currencyId = state.selectedCurrencyId ?: return@launch,
                        initialBalance = balanceValue
                    )
                } else {
                    repository.addAccount(
                        name = state.name.trim(),
                        type = state.selectedType,
                        currencyId = state.selectedCurrencyId ?: return@launch,
                        initialBalance = balanceValue,
                        icon = null,
                        colour = null,
                        userId = defaultUserId
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = t.localizedMessage ?: "Unable to save account"
                )
            }
        }
    }
}
