package com.andydel.financemanager.domain.model

data class SummarySnapshot(
    val currentBalance: Double,
    val savingsBalance: Double,
    val debtBalance: Double,
    val totalInvestments: Double,
    val totalDebt: Double
) {
    val netWorth: Double get() = totalInvestments - totalDebt
}
