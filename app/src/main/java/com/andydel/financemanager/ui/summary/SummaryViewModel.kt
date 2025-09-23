package com.andydel.financemanager.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andydel.financemanager.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SummaryViewModel(repository: FinanceRepository) : ViewModel() {
    val uiState = repository.observeSummary()
        .map<_, SummaryUiState> { summary -> SummaryUiState.Success(summary) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState.Loading
        )
}
