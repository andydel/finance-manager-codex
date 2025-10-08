package com.andydel.financemanager.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andydel.financemanager.domain.model.Currency

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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        snapshot.baseCurrency?.let { currency ->
            Text(
                text = "All values shown in ${currency.name} (${currency.code}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SummaryCard(title = "Current accounts", amount = snapshot.currentBalance, baseCurrency = snapshot.baseCurrency)
        SummaryCard(title = "Savings & Investments", amount = snapshot.savingsBalance, baseCurrency = snapshot.baseCurrency)
        SummaryCard(title = "Debt", amount = snapshot.debtBalance, baseCurrency = snapshot.baseCurrency)
        SummaryCard(title = "Total assets", amount = snapshot.totalAssets, baseCurrency = snapshot.baseCurrency)
        SummaryCard(title = "Total debt", amount = snapshot.totalDebt, baseCurrency = snapshot.baseCurrency)
        SummaryCard(title = "Net worth", amount = snapshot.netWorth, baseCurrency = snapshot.baseCurrency, emphasise = true)
    }
}

@Composable
private fun SummaryCard(title: String, amount: Double, baseCurrency: Currency?, emphasise: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = formatAmount(baseCurrency, amount),
                style = if (emphasise) MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                else MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatAmount(currency: Currency?, amount: Double): String {
    val formatted = "%,.2f".format(amount)
    return if (currency == null) {
        formatted
    } else {
        "${currency.symbol}$formatted"
    }
}
