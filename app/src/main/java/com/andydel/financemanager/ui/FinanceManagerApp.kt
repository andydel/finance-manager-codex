package com.andydel.financemanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.andydel.financemanager.ui.addtransaction.AddTransactionScreen
import com.andydel.financemanager.ui.addtransaction.AddTransactionViewModel
import com.andydel.financemanager.ui.components.FinanceTopBar
import com.andydel.financemanager.ui.home.HomeScreen
import com.andydel.financemanager.ui.navigation.FinanceDestination
import com.andydel.financemanager.ui.settings.SettingsScreen
import com.andydel.financemanager.ui.summary.SummaryScreen

@Composable
fun FinanceManagerApp(repository: FinanceRepository) {
    val navController = rememberNavController()
    val factory = remember(repository) { FinanceViewModelFactory(repository) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?") ?: FinanceDestination.Home.route

    Scaffold(
        topBar = {
            FinanceTopBar(
                title = when (currentRoute) {
                    FinanceDestination.Home.route -> "Finance Manager"
                    FinanceDestination.AddAccount.route -> "Add Account"
                    "addTransaction" -> "Add Transaction"
                    FinanceDestination.Summary.route -> "Summary"
                    FinanceDestination.Settings.route -> "Settings"
                    else -> "Finance Manager"
                },
                onAddAccount = { navController.navigate(FinanceDestination.AddAccount.route) },
                onShowSummary = { navController.navigate(FinanceDestination.Summary.route) },
                onShowSettings = { navController.navigate(FinanceDestination.Settings.route) }
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
                    }
                )
            }
            composable(FinanceDestination.AddAccount.route) {
                val viewModel: com.andydel.financemanager.ui.addaccount.AddAccountViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()

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
                    }
                )
            ) { entry ->
                val viewModel: com.andydel.financemanager.ui.addtransaction.AddTransactionViewModel = viewModel(factory = factory)
                val state by viewModel.uiState.collectAsState()
                val passedAccountId = entry.arguments?.getLong(FinanceDestination.TRANSACTION_ACCOUNT_ID_KEY)?.takeIf { it != -1L }

                LaunchedEffect(passedAccountId) {
                    viewModel.setDefaultAccount(passedAccountId)
                }

                AddTransactionScreen(
                    state = state,
                    onAmountChanged = viewModel::onAmountChanged,
                    onAccountSelected = viewModel::onAccountSelected,
                    onCategorySelected = viewModel::onCategorySelected,
                    onTransactionTypeChange = viewModel::onTransactionTypeChange,
                    onSave = { viewModel.saveTransaction() },
                    onClose = { navController.popBackStack() }
                )

                LaunchedEffect(state.saved) {
                    if (state.saved) {
                        navController.popBackStack()
                    }
                }
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
                    onSave = viewModel::saveSettings
                )
            }
        }
    }
}
