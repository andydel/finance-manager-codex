package com.andydel.financemanager.data.repository

import com.andydel.financemanager.data.local.entities.AccountEntity
import com.andydel.financemanager.data.local.entities.CategoryEntity
import com.andydel.financemanager.data.local.entities.CurrencyEntity
import com.andydel.financemanager.data.local.entities.TransactionEntity
import com.andydel.financemanager.data.local.entities.UserEntity
import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.AccountType
import com.andydel.financemanager.domain.model.Category
import com.andydel.financemanager.domain.model.Currency
import com.andydel.financemanager.domain.model.FinanceTransaction
import com.andydel.financemanager.domain.model.TransactionType
import com.andydel.financemanager.domain.model.UserProfile

fun CurrencyEntity.toDomain(): Currency = Currency(
    id = id,
    name = name,
    symbol = symbol
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name
)

fun TransactionEntity.toDomain(): FinanceTransaction = FinanceTransaction(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amount = amount,
    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
    timestamp = date
)

fun AccountEntity.toDomain(
    currency: Currency,
    transactions: List<TransactionEntity>
): Account {
    val netChange = transactions.sumOf { entity ->
        if (entity.isIncome) entity.amount else -entity.amount
    }
    return Account(
        id = id,
        name = name,
        type = AccountType.fromRaw(type),
        currency = currency,
        initialBalance = initialBalance,
        currentBalance = initialBalance + netChange,
        icon = icon,
        color = colour
    )
}

fun UserEntity.toDomain(currency: Currency): UserProfile = UserProfile(
    id = id,
    name = name,
    baseCurrency = currency
)
