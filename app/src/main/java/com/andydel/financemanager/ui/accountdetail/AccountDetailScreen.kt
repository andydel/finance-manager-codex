package com.andydel.financemanager.ui.accountdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AccountDetailScreen(
    state: AccountDetailUiState,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    when {
        state.accountMissing -> MissingAccount()
        state.isLoading -> LoadingState()
        else -> AccountDetailContent(state, onSearchQueryChange, onClearSearch)
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Loading account...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MissingAccount() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account not found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The requested account could not be located.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AccountDetailContent(
    state: AccountDetailUiState,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    val amountFormatter = rememberFormatter()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = state.accountName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "Balance: ${amountFormatter.formatWithSymbol(state.currencySymbol, state.currentBalance)}",
            style = MaterialTheme.typography.titleMedium
        )

        SearchField(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onClear = onClearSearch
        )

        Divider()

        if (state.transactions.isEmpty()) {
            Text(
                text = "No transactions yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val dateFormatter = rememberDateFormatter()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        currencySymbol = state.currencySymbol,
                        amountFormatter = amountFormatter,
                        dateFormatter = dateFormatter
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Search description") },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        }
    )
}

@Composable
private fun TransactionRow(
    transaction: AccountTransactionItem,
    currencySymbol: String,
    amountFormatter: AmountFormatter,
    dateFormatter: DateTimeFormatter
) {
    val dateText = dateFormatter.format(transaction.timestamp.atZone(ZoneId.systemDefault()))
    val amountValue = if (transaction.isIncome) transaction.amount else -transaction.amount
    val amountColor = if (transaction.isIncome) Color(0xFF2E7D32) else Color.Black

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountFormatter.formatWithSymbol(currencySymbol, amountValue),
                    style = MaterialTheme.typography.bodyLarge,
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Balance: ${amountFormatter.formatWithSymbol(currencySymbol, transaction.runningBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(): DateTimeFormatter = remember {
    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
}

private class AmountFormatter(private val pattern: java.text.DecimalFormat) {
    fun format(amount: Double): String = pattern.format(amount)
    fun formatWithSymbol(symbol: String, amount: Double): String = "$symbol${format(amount)}"
}

@Composable
private fun rememberFormatter(): AmountFormatter {
    val decimalFormat = remember {
        (java.text.NumberFormat.getNumberInstance(Locale.getDefault()) as java.text.DecimalFormat).apply {
            applyPattern("#,##0.00")
        }
    }
    return remember(decimalFormat) { AmountFormatter(decimalFormat) }
}
