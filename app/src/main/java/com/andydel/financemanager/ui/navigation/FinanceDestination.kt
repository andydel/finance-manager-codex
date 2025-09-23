package com.andydel.financemanager.ui.navigation

sealed class FinanceDestination(val route: String) {
    data object Home : FinanceDestination("home")
    data object AddAccount : FinanceDestination("addAccount")
    data object AddTransaction : FinanceDestination("addTransaction?accountId={accountId}")
    data object Summary : FinanceDestination("summary")
    data object Settings : FinanceDestination("settings")

    companion object {
        const val TRANSACTION_ACCOUNT_ID_KEY = "accountId"
        fun addTransactionRoute(accountId: Long?): String =
            if (accountId != null) "addTransaction?accountId=$accountId" else "addTransaction?accountId=-1"
    }
}
