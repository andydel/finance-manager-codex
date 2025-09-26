package com.andydel.financemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andydel.financemanager.data.local.entities.AccountEntity
import com.andydel.financemanager.data.local.models.AccountWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM accounts ORDER BY position ASC, name ASC")
    fun observeAccounts(): Flow<List<AccountWithTransactions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COALESCE(MAX(position), -1) FROM accounts WHERE type = :type")
    suspend fun getMaxPositionForType(type: String): Int

    @Query("UPDATE accounts SET position = :position WHERE id = :accountId")
    suspend fun updatePosition(accountId: Long, position: Int)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun delete(accountId: Long)
}
