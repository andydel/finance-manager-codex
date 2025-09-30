package com.andydel.financemanager.domain.model

enum class AccountType(val displayName: String) {
    CURRENT("Current"),
    SAVINGS("Savings & Investments"),
    DEBT("Debt");

    fun balanceImpact(isIncome: Boolean, amount: Double): Double = when (this) {
        DEBT -> if (isIncome) -amount else amount
        else -> if (isIncome) amount else -amount
    }

    companion object {
        fun fromRaw(raw: String): AccountType = values().firstOrNull {
            it.name.equals(raw, ignoreCase = true)
        } ?: CURRENT
    }
}
