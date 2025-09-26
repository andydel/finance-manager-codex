package com.andydel.financemanager.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import com.andydel.financemanager.data.local.entities.AccountEntity
import com.andydel.financemanager.data.local.entities.TransactionEntity

data class AccountWithTransactions(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "account"
    )
    val transactions: List<TransactionEntity>
)
