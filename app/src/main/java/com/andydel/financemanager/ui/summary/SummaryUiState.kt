package com.andydel.financemanager.ui.summary

import com.andydel.financemanager.domain.model.SummarySnapshot

sealed interface SummaryUiState {
    data object Loading : SummaryUiState
    data class Success(val snapshot: SummarySnapshot) : SummaryUiState
}
