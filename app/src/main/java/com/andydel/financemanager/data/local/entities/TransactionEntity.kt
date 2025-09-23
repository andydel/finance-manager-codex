package com.andydel.financemanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val date: Instant,
    @ColumnInfo(name = "category") val categoryId: Long,
    @ColumnInfo(name = "account") val accountId: Long,
    @ColumnInfo(name = "is_income") val isIncome: Boolean
)
