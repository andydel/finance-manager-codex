package com.andydel.financemanager.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.AccountType
import com.andydel.financemanager.ui.home.AccountManagementChange

private data class TabDefinition(val type: AccountType, val title: String)

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddTransaction: (Long?) -> Unit,
    onOpenAccount: (Long) -> Unit,
    onEditAccount: (Long) -> Unit,
    isManaging: Boolean,
    onExitManage: () -> Unit,
    onApplyAccountChanges: (AccountManagementChange) -> Unit,
    registerManageActions: (onDone: () -> Unit, onCancel: () -> Unit) -> Unit
) {
    val tabs = remember {
        listOf(
            TabDefinition(AccountType.CURRENT, "Current"),
            TabDefinition(AccountType.SAVINGS, "Savings & Investments"),
            TabDefinition(AccountType.DEBT, "Debt")
        )
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val managedCurrent = remember { mutableStateListOf<Account>() }
    val managedSavings = remember { mutableStateListOf<Account>() }
    val managedDebt = remember { mutableStateListOf<Account>() }
    val deletedAccountIds = remember { mutableStateListOf<Long>() }
    var accountPendingDelete by remember { mutableStateOf<Account?>(null) }

    fun resetManagedState() {
        managedCurrent.clear()
        managedSavings.clear()
        managedDebt.clear()
        deletedAccountIds.clear()
        accountPendingDelete = null
    }

    fun syncManagedLists() {
        managedCurrent.apply {
            clear()
            addAll(state.currentAccounts)
        }
        managedSavings.apply {
            clear()
            addAll(state.savingsAccounts)
        }
        managedDebt.apply {
            clear()
            addAll(state.debtAccounts)
        }
        deletedAccountIds.clear()
    }

    fun activeAccountsFor(type: AccountType): List<Account> = when (type) {
        AccountType.CURRENT -> managedCurrent.filterNot { deletedAccountIds.contains(it.id) }
        AccountType.SAVINGS -> managedSavings.filterNot { deletedAccountIds.contains(it.id) }
        AccountType.DEBT -> managedDebt.filterNot { deletedAccountIds.contains(it.id) }
    }

    fun performDelete(account: Account) {
        if (!deletedAccountIds.contains(account.id)) {
            deletedAccountIds.add(account.id)
        }
        when (account.type) {
            AccountType.CURRENT -> managedCurrent.removeAll { it.id == account.id }
            AccountType.SAVINGS -> managedSavings.removeAll { it.id == account.id }
            AccountType.DEBT -> managedDebt.removeAll { it.id == account.id }
        }
    }

    fun handleCancel() {
        resetManagedState()
        onExitManage()
    }

    fun handleDone() {
        val reorderMap = mapOf(
            AccountType.CURRENT to activeAccountsFor(AccountType.CURRENT).map(Account::id),
            AccountType.SAVINGS to activeAccountsFor(AccountType.SAVINGS).map(Account::id),
            AccountType.DEBT to activeAccountsFor(AccountType.DEBT).map(Account::id)
        )
        val change = AccountManagementChange(
            reorderedAccountIdsByType = reorderMap,
            deletedAccountIds = deletedAccountIds.toList()
        )
        onApplyAccountChanges(change)
        resetManagedState()
        onExitManage()
    }

    LaunchedEffect(isManaging) {
        if (isManaging) {
            syncManagedLists()
            registerManageActions(::handleDone, ::handleCancel)
        } else {
            resetManagedState()
            registerManageActions({}, {})
        }
    }

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
                if (isManaging) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Use the arrows to reorder accounts and the bin icon to delete. Changes apply when you tap Done.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                val accountsForTab = when (tabs[selectedTab].type) {
                    AccountType.CURRENT -> if (isManaging) managedCurrent else state.currentAccounts
                    AccountType.SAVINGS -> if (isManaging) managedSavings else state.savingsAccounts
                    AccountType.DEBT -> if (isManaging) managedDebt else state.debtAccounts
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
                    if (isManaging) {
                        val onMove: (Int, Int) -> Unit = when (tabs[selectedTab].type) {
                            AccountType.CURRENT -> { from, to -> moveAccount(managedCurrent, from, to) }
                            AccountType.SAVINGS -> { from, to -> moveAccount(managedSavings, from, to) }
                            AccountType.DEBT -> { from, to -> moveAccount(managedDebt, from, to) }
                        }
                        AccountManageList(
                            accounts = accountsForTab,
                            modifier = Modifier.fillMaxSize(),
                            onMove = onMove,
                            onDelete = { accountPendingDelete = it }
                        )
                    } else {
                        AccountDisplayList(
                            accounts = accountsForTab,
                            modifier = Modifier.fillMaxSize(),
                            onAccountOpen = onOpenAccount,
                            onAccountEdit = onEditAccount
                        )
                    }
                }
            }
        }

        if (!isManaging) {
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

    if (accountPendingDelete != null) {
        val accountToDelete = accountPendingDelete!!
        AlertDialog(
            onDismissRequest = { accountPendingDelete = null },
            title = { Text(text = "Delete ${accountToDelete.name}?") },
            text = {
                Text("This will remove the account and its transactions. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    performDelete(accountToDelete)
                    accountPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { accountPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountDisplayList(
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
        itemsIndexed(accounts, key = { _, account -> account.id }) { _, account ->
            AccountDisplayCard(
                account = account,
                onOpen = { onAccountOpen(account.id) },
                onEdit = { onAccountEdit(account.id) }
            )
        }
    }
}

@Composable
private fun AccountManageList(
    accounts: List<Account>,
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit,
    onDelete: (Account) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(accounts, key = { _, account -> account.id }) { index, account ->
            val canMoveUp = index > 0
            val canMoveDown = index < accounts.lastIndex
            AccountManageRow(
                account = account,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onMoveUp = { onMove(index, index - 1) },
                onMoveDown = { onMove(index, index + 1) },
                onDelete = { onDelete(account) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AccountDisplayCard(
    account: Account,
    onOpen: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onEdit
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AccountCardContent(account = account)
    }
}

@Composable
private fun AccountManageRow(
    account: Account,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AccountCardContent(account = account)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move down")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete account")
                }
            }
        }
    }
}

@Composable
private fun AccountCardContent(account: Account) {
    Column {
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

private fun moveAccount(list: MutableList<Account>, from: Int, to: Int) {
    if (from == to || from !in list.indices || to !in list.indices) return
    val item = list.removeAt(from)
    list.add(to, item)
}
