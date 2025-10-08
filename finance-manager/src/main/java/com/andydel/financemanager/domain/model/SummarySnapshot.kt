package com.andydel.financemanager.domain.model

data class SummarySnapshot(
    val currentBalance: Double,
    val savingsBalance: Double,
    val debtBalance: Double,
    val totalAssets: Double,
    val totalDebt: Double,
    val baseCurrency: Currency?
) {
    val netWorth: Double get() = totalAssets - totalDebt
}
