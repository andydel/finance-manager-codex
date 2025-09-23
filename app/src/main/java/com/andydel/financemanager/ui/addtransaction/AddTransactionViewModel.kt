package com.andydel.financemanager.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    init {
        observeAccounts()
        observeCategories()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            repository.observeAccounts().collectLatest { accounts ->
                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    selectedAccountId = _uiState.value.selectedAccountId ?: accounts.firstOrNull()?.id
                )
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.observeCategories().collectLatest { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    selectedCategoryId = _uiState.value.selectedCategoryId ?: categories.firstOrNull()?.id
                )
            }
        }
    }

    fun setDefaultAccount(accountId: Long?) {
        if (accountId == null) return
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onAccountSelected(accountId: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onTransactionTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(transactionType = type)
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        val amountValue = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, errorMessage = null)
                repository.addTransaction(
                    accountId = state.selectedAccountId ?: return@launch,
                    categoryId = state.selectedCategoryId ?: return@launch,
                    amount = amountValue,
                    type = state.transactionType
                )
                _uiState.value = _uiState.value.copy(isSaving = false, saved = true, amount = "")
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = t.localizedMessage ?: "Unable to save transaction"
                )
            }
        }
    }
}
