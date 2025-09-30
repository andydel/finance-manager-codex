package com.andydel.financemanager.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
        val current = _uiState.value
        if (current.editingTransactionId != null) return
        _uiState.value = current.copy(selectedAccountId = accountId)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
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

    fun onTransactionDateSelected(instant: Instant) {
        _uiState.value = _uiState.value.copy(transactionInstant = instant)
    }

    fun startEditing(transactionId: Long) {
        if (_uiState.value.editingTransactionId == transactionId) return
        viewModelScope.launch {
            val transaction = repository.getTransaction(transactionId) ?: return@launch
            val amountString = BigDecimal.valueOf(transaction.amount)
                .stripTrailingZeros()
                .toPlainString()
            _uiState.value = _uiState.value.copy(
                editingTransactionId = transaction.id,
                selectedAccountId = transaction.accountId,
                selectedCategoryId = transaction.categoryId,
                amount = amountString,
                description = transaction.description,
                transactionInstant = transaction.timestamp,
                transactionType = transaction.type,
                isSaving = false,
                errorMessage = null,
                saved = false
            )
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        val amountValue = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, errorMessage = null)
                val accountId = state.selectedAccountId ?: return@launch
                val categoryId = state.selectedCategoryId ?: return@launch
                val trimmedDescription = state.description.trim()
                val transactionId = state.editingTransactionId

                if (transactionId != null) {
                    repository.updateTransaction(
                        transactionId = transactionId,
                        accountId = accountId,
                        categoryId = categoryId,
                        description = trimmedDescription,
                        amount = amountValue,
                        type = state.transactionType,
                        timestamp = state.transactionInstant
                    )
                    _uiState.value = state.copy(
                        isSaving = false,
                        saved = true
                    )
                } else {
                    repository.addTransaction(
                        accountId = accountId,
                        categoryId = categoryId,
                        description = trimmedDescription,
                        amount = amountValue,
                        type = state.transactionType,
                        timestamp = state.transactionInstant
                    )
                    _uiState.value = state.copy(
                        isSaving = false,
                        saved = true,
                        amount = "",
                        description = "",
                        transactionInstant = ZonedDateTime.now(ZoneOffset.UTC).toInstant()
                    )
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = t.localizedMessage ?: "Unable to save transaction"
                )
            }
        }
    }
}
