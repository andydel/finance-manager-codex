package com.andydel.financemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andydel.financemanager.data.local.dao.AccountDao
import com.andydel.financemanager.data.local.dao.CategoryDao
import com.andydel.financemanager.data.local.dao.CurrencyDao
import com.andydel.financemanager.data.local.dao.TransactionDao
import com.andydel.financemanager.data.local.dao.UserDao
import com.andydel.financemanager.data.local.entities.AccountEntity
import com.andydel.financemanager.data.local.entities.CategoryEntity
import com.andydel.financemanager.data.local.entities.CurrencyEntity
import com.andydel.financemanager.data.local.entities.TransactionEntity
import com.andydel.financemanager.data.local.entities.UserEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        UserEntity::class,
        CurrencyEntity::class
    ],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao
    abstract fun currencyDao(): CurrencyDao

    companion object {
        const val DATABASE_NAME = "finance_manager.db"
    }
}
