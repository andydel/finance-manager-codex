package com.andydel.financemanager.domain.model

enum class AccountType(val displayName: String) {
    CURRENT("Current"),
    SAVINGS("Savings & Investments"),
    DEBT("Debt");

    companion object {
        fun fromRaw(raw: String): AccountType = values().firstOrNull {
            it.name.equals(raw, ignoreCase = true)
        } ?: CURRENT
    }
}
