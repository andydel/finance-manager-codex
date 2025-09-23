package com.andydel.financemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.ui.addaccount.AddAccountViewModel
import com.andydel.financemanager.ui.addtransaction.AddTransactionViewModel
import com.andydel.financemanager.ui.home.HomeViewModel
import com.andydel.financemanager.ui.settings.SettingsViewModel
import com.andydel.financemanager.ui.summary.SummaryViewModel

class FinanceViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository)
        modelClass.isAssignableFrom(AddAccountViewModel::class.java) -> AddAccountViewModel(repository)
        modelClass.isAssignableFrom(AddTransactionViewModel::class.java) -> AddTransactionViewModel(repository)
        modelClass.isAssignableFrom(SummaryViewModel::class.java) -> SummaryViewModel(repository)
        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository)
        else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    } as T
}
