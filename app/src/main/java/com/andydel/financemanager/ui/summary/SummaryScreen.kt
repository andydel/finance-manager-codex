package com.andydel.financemanager.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SummaryScreen(state: SummaryUiState) {
    when (state) {
        SummaryUiState.Loading -> LoadingSummary()
        is SummaryUiState.Success -> SummaryContent(state)
    }
}

@Composable
private fun LoadingSummary() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Loading summary...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryContent(state: SummaryUiState.Success) {
    val snapshot = state.snapshot
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(title = "Current accounts", amount = snapshot.currentBalance)
        SummaryCard(title = "Savings & Investments", amount = snapshot.savingsBalance)
        SummaryCard(title = "Debt", amount = snapshot.debtBalance)
        SummaryCard(title = "Total investments", amount = snapshot.totalInvestments)
        SummaryCard(title = "Total debt", amount = snapshot.totalDebt)
        SummaryCard(title = "Net worth", amount = snapshot.netWorth, emphasise = true)
    }
}

@Composable
private fun SummaryCard(title: String, amount: Double, emphasise: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%,.2f".format(amount)}",
                style = if (emphasise) MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                else MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
