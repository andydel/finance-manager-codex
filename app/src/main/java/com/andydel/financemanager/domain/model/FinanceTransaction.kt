package com.andydel.financemanager.domain.model

import java.time.Instant

data class FinanceTransaction(
    val id: Long = 0L,
    val accountId: Long,
    val categoryId: Long,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Instant
)
