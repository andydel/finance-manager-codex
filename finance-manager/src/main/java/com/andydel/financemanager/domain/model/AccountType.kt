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
        private val lookup: Map<String, AccountType> = buildMap {
            values().forEach { type ->
                normalise(type.name)?.let { put(it, type) }
                normalise(type.displayName)?.let { put(it, type) }
            }
            // Provide a couple of common aliases that appear in older exports.
            putIfAbsent("checking", CURRENT)
            putIfAbsent("savingsandinvestment", SAVINGS)
            putIfAbsent("liability", DEBT)
        }

        fun fromRaw(raw: String): AccountType {
            val normalised = normalise(raw) ?: return CURRENT
            return lookup[normalised] ?: CURRENT
        }

        private fun normalise(value: String): String? {
            val cleaned = value.trim()
                .lowercase()
                .filter { it.isLetterOrDigit() }
            return cleaned.ifEmpty { null }
        }
    }
}
