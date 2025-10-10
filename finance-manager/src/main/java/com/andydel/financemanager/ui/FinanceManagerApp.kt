package com.andydel.financemanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andydel.financemanager.data.repository.FinanceRepository
import com.andydel.financemanager.ui.addaccount.AddAccountScreen
import com.andydel.financemanager.ui.accountdetail.AccountDetailScreen
import com.andydel.financemanager.ui.accountdetail.AccountDetailViewModel
import com.andydel.financemanager.ui.addtransaction.AddTransactionScreen
import com.andydel.financemanager.ui.addtransaction.AddTransactionViewModel
import com.andydel.financemanager.ui.components.FinanceTopBar
import com.andydel.financemanager.ui.home.HomeScreen
import com.andydel.financemanager.ui.home.AccountManagementChange
import com.andydel.financemanager.ui.navigation.FinanceDestination
import com.andydel.financemanager.ui.settings.SettingsScreen
import com.andydel.financemanager.ui.summary.SummaryScreen

@Composable
fun FinanceManagerApp(repository: FinanceRepository) {
    val navController = rememberNavController()
    val factory = remember(repository) { FinanceViewModelFactory(repository) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinationRoute = backStackEntry?.destination?.route
    val currentRoute = destinationRoute?.substringBefore("?") ?: FinanceDestination.Home.route
    val addAccountArgument = backStackEntry?.arguments?.getLong(FinanceDestination.ACCOUNT_FORM_ACCOUNT_ID_KEY)?.takeIf { it > 0 }
    val editTransactionId = backStackEntry?.arguments?.getLong(FinanceDestination.TRANSACTION_ID_KEY)?.takeIf { it > 0 }
    var isManagingAccounts by rememberSaveable { mutableStateOf(false) }
    var manageDoneAction by remember { mutableStateOf<() -> Unit>({}) }
    var manageCancelAction by remember { mutableStateOf<() -> Unit>({}) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != FinanceDestination.Home.route && isManagingAccounts) {
            isManagingAccounts = false
            manageDoneAction = {}
            manageCancelAction = {}
        }
    }

    Scaffold(
        topBar = {
            FinanceTopBar(
                title = when (currentRoute) {
                    FinanceDestination.Home.route -> if (isManagingAccounts) "Manage Accounts" else "Finance Manager"
                    FinanceDestination.AddAccount.route.substringBefore("?") ->
                        if (addAccountArgument != null) "Edit Account" else "Add Account"
                    "addTransaction" -> if (editTransactionId != null) "Edit Transaction" else "Add Transaction"
                    FinanceDestination.Summary.route -> "Summary"
                    FinanceDestination.Settings.route -> "Settings"
                    FinanceDestination.AccountDetail.route -> "Account Details"
                    else -> "Finance Manager"
                },
                onNavigateHome = {
                    navController.navigate(FinanceDestination.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onAddAccount = { navController.navigate(FinanceDestination.addAccountRoute()) },
                onShowSummary = { navController.navigate(FinanceDestination.Summary.route) },
                onShowSettings = { navController.navigate(FinanceDestination.Settings.route) },
                showManageAccounts = currentRoute == FinanceDestination.Home.route && !isManagingAccounts,
                isManagingAccounts = isManagingAccounts,
                onManageAccounts = {
                    isManagingAccounts = true
                },
                onDoneManaging = if (isManagingAccounts) manageDoneAction else null,
                onCancelManaging = if (isManagingAccounts) manageCancelAction else null
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = FinanceDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(FinanceDestination.Home.route) {
                val viewModel: com.andydel.financemanager.ui.home.HomeViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()

                HomeScreen(
                    state = state,
                    onAddTransaction = { accountId ->
                        navController.navigate(FinanceDestination.addTransactionRoute(accountId))
                    },
                    onOpenAccount = { accountId ->
                        navController.navigate(FinanceDestination.accountDetailRoute(accountId))
                    },
                    onEditAccount = { accountId ->
                        navController.navigate(FinanceDestination.addAccountRoute(accountId))
                    },
                    isManaging = isManagingAccounts,
                    onExitManage = { isManagingAccounts = false },
                    onApplyAccountChanges = { change -> viewModel.applyAccountManagement(change) },
                    registerManageActions = { done, cancel ->
                        manageDoneAction = done
                        manageCancelAction = cancel
                    }
                )
            }
            composable(
                route = FinanceDestination.AddAccount.route,
                arguments = listOf(
                    navArgument(FinanceDestination.ACCOUNT_FORM_ACCOUNT_ID_KEY) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val viewModel: com.andydel.financemanager.ui.addaccount.AddAccountViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()
                val editingAccountId = entry.arguments?.getLong(FinanceDestination.ACCOUNT_FORM_ACCOUNT_ID_KEY)?.takeIf { it > 0 }

                LaunchedEffect(editingAccountId) {
                    viewModel.loadAccountForEdit(editingAccountId)
                }

                AddAccountScreen(
                    state = state,
                    onNameChanged = viewModel::onNameChanged,
                    onBalanceChanged = viewModel::onBalanceChanged,
                    onTypeSelected = viewModel::onTypeSelected,
                    onCurrencySelected = viewModel::onCurrencySelected,
                    onSave = { viewModel.saveAccount() },
                    onClose = { navController.popBackStack() }
                )

                LaunchedEffect(state.saved) {
                    if (state.saved) {
                        navController.popBackStack()
                    }
                }
            }
            composable(
                route = FinanceDestination.AddTransaction.route,
                arguments = listOf(
                    navArgument(FinanceDestination.TRANSACTION_ACCOUNT_ID_KEY) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(FinanceDestination.TRANSACTION_ID_KEY) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val viewModel: com.andydel.financemanager.ui.addtransaction.AddTransactionViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()
                val passedAccountId = entry.arguments?.getLong(FinanceDestination.TRANSACTION_ACCOUNT_ID_KEY)?.takeIf { it != -1L }
                val editingTransactionId = entry.arguments?.getLong(FinanceDestination.TRANSACTION_ID_KEY)?.takeIf { it != -1L }

                LaunchedEffect(passedAccountId) {
                    viewModel.setDefaultAccount(passedAccountId)
                }

                LaunchedEffect(editingTransactionId) {
                    if (editingTransactionId != null) {
                        viewModel.startEditing(editingTransactionId)
                    }
                }

                AddTransactionScreen(
                    state = state,
                    onAmountChanged = viewModel::onAmountChanged,
                    onDescriptionChanged = viewModel::onDescriptionChanged,
                    onAccountSelected = viewModel::onAccountSelected,
                    onCategorySelected = viewModel::onCategorySelected,
                    onTransactionTypeChange = viewModel::onTransactionTypeChange,
                    onTransactionDateSelected = viewModel::onTransactionDateSelected,
                    onSave = { viewModel.saveTransaction() },
                    onClose = { navController.popBackStack() }
                )

                LaunchedEffect(state.saved) {
                    if (state.saved) {
                        navController.popBackStack()
                    }
                }
            }
            composable(
                route = FinanceDestination.AccountDetail.route,
                arguments = listOf(
                    navArgument(FinanceDestination.ACCOUNT_DETAIL_ACCOUNT_ID_KEY) {
                        type = NavType.LongType
                    }
                )
            ) { entry ->
                val accountId = entry.arguments?.getLong(FinanceDestination.ACCOUNT_DETAIL_ACCOUNT_ID_KEY)
                if (accountId == null) {
                    return@composable
                }

                val viewModel: AccountDetailViewModel = viewModel(
                    factory = AccountDetailViewModel.provideFactory(repository, accountId)
                )
                val state by viewModel.uiState.collectAsState()

                AccountDetailScreen(
                    state = state,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onClearSearch = viewModel::clearSearch,
                    onTransactionClick = { transactionId ->
                        navController.navigate(
                            FinanceDestination.addTransactionRoute(
                                accountId = accountId,
                                transactionId = transactionId
                            )
                        )
                    },
                    onDeleteTransaction = viewModel::deleteTransaction
                )
            }
            composable(FinanceDestination.Summary.route) {
                val viewModel: com.andydel.financemanager.ui.summary.SummaryViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()
                SummaryScreen(state = state)
            }
            composable(FinanceDestination.Settings.route) {
                val viewModel: com.andydel.financemanager.ui.settings.SettingsViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()
                SettingsScreen(
                    state = state,
                    onNameChanged = viewModel::onNameChanged,
                    onCurrencySelected = viewModel::onCurrencySelected,
                    onExchangeRateApiKeyChanged = viewModel::onExchangeRateApiKeyChanged,
                    onSave = viewModel::saveSettings
                )
            }
        }
    }
}
