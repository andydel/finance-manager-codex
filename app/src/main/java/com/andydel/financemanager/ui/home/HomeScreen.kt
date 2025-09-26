package com.andydel.financemanager.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.AccountType

private data class TabDefinition(val type: AccountType, val title: String)

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddTransaction: (Long?) -> Unit,
    onOpenAccount: (Long) -> Unit,
    onEditAccount: (Long) -> Unit
) {
    val tabs = remember {
        listOf(
            TabDefinition(AccountType.CURRENT, "Current"),
            TabDefinition(AccountType.SAVINGS, "Savings & Investments"),
            TabDefinition(AccountType.DEBT, "Debt")
        )
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        text = { Text(tab.title) }
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val accountsForTab = when (tabs[selectedTab].type) {
                    AccountType.CURRENT -> state.currentAccounts
                    AccountType.SAVINGS -> state.savingsAccounts
                    AccountType.DEBT -> state.debtAccounts
                }

                if (accountsForTab.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No accounts yet. Use the menu to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AccountList(
                        accounts = accountsForTab,
                        modifier = Modifier.fillMaxSize(),
                        onAccountOpen = onOpenAccount,
                        onAccountEdit = onEditAccount
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val defaultAccountId = when (tabs[selectedTab].type) {
                    AccountType.CURRENT -> state.currentAccounts.firstOrNull()?.id
                    AccountType.SAVINGS -> state.savingsAccounts.firstOrNull()?.id
                    AccountType.DEBT -> state.debtAccounts.firstOrNull()?.id
                }
                onAddTransaction(defaultAccountId)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add transaction")
        }
    }
}

@Composable
private fun AccountList(
    accounts: List<Account>,
    modifier: Modifier = Modifier,
    onAccountOpen: (Long) -> Unit,
    onAccountEdit: (Long) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(accounts, key = Account::id) { account ->
            AccountCard(
                account = account,
                onOpen = { onAccountOpen(account.id) },
                onEdit = { onAccountEdit(account.id) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AccountCard(account: Account, onOpen: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onEdit
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${account.currency.symbol}${"%,.2f".format(account.currentBalance)}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = "Initial balance: ${account.currency.symbol}${"%,.2f".format(account.initialBalance)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
