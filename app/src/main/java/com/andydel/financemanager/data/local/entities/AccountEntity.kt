package com.andydel.financemanager.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "icon") val icon: String?,
    @ColumnInfo(name = "colour") val colour: String?,
    @ColumnInfo(name = "currency") val currencyId: Long,
    @ColumnInfo(name = "initial_balance") val initialBalance: Double,
    @ColumnInfo(name = "user_id") val userId: Long?
)
