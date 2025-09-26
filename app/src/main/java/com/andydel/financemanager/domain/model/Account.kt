package com.andydel.financemanager.domain.model

data class Account(
    val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val currency: Currency,
    val initialBalance: Double,
    val currentBalance: Double,
    val icon: String?,
    val color: String?,
    val sortOrder: Int
)
