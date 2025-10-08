package com.andydel.financemanager

import android.content.Context
import androidx.room.Room
import com.andydel.financemanager.data.local.AppDatabase
import com.andydel.financemanager.data.remote.ExchangeRateApiKeyProvider
import com.andydel.financemanager.data.remote.ExchangeRateService
import com.andydel.financemanager.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    private val exchangeRateService = ExchangeRateService(
        apiKeyProvider = ExchangeRateApiKeyProvider(context)
    )

    val repository: FinanceRepository = FinanceRepository(
        accountDao = database.accountDao(),
        transactionDao = database.transactionDao(),
        categoryDao = database.categoryDao(),
        currencyDao = database.currencyDao(),
        userDao = database.userDao(),
        exchangeRateService = exchangeRateService
    )

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        applicationScope.launch {
            repository.ensureSeedData()
        }
    }
}
