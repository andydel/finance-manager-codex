package com.andydel.financemanager.data.repository

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
import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.AccountType
import com.andydel.financemanager.domain.model.Category
import com.andydel.financemanager.domain.model.Currency
import com.andydel.financemanager.domain.model.FinanceTransaction
import com.andydel.financemanager.domain.model.SummarySnapshot
import com.andydel.financemanager.domain.model.TransactionType
import com.andydel.financemanager.domain.model.UserProfile
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val currencyDao: CurrencyDao,
    private val userDao: UserDao
) {

    fun observeAccounts(): Flow<List<Account>> = combine(
        accountDao.observeAccounts(),
        currencyDao.observeCurrencies()
    ) { accounts, currencies ->
        val currencyMap = currencies.associateBy(CurrencyEntity::id)
        accounts.mapNotNull { accountWithTransactions ->
            val currencyEntity = currencyMap[accountWithTransactions.account.currencyId] ?: return@mapNotNull null
            accountWithTransactions.account.toDomain(
                currency = currencyEntity.toDomain(),
                transactions = accountWithTransactions.transactions
            )
        }.sortedBy(Account::sortOrder)
    }

    fun observeAccounts(type: AccountType): Flow<List<Account>> = observeAccounts().map { list ->
        list.filter { it.type == type }
    }

    fun observeTransactions(accountId: Long): Flow<List<FinanceTransaction>> =
        transactionDao.observeForAccount(accountId).map { entities ->
            entities.map(TransactionEntity::toDomain)
        }

    fun observeSummary(): Flow<SummarySnapshot> = observeAccounts().map { accounts ->
        val current = accounts.filter { it.type == AccountType.CURRENT }.sumOf(Account::currentBalance)
        val savings = accounts.filter { it.type == AccountType.SAVINGS }.sumOf(Account::currentBalance)
        val debt = accounts.filter { it.type == AccountType.DEBT }.sumOf(Account::currentBalance)
        SummarySnapshot(
            currentBalance = current,
            savingsBalance = savings,
            debtBalance = debt,
            totalAssets = current + savings,
            totalDebt = debt
        )
    }

    fun observeCurrencies(): Flow<List<Currency>> = currencyDao.observeCurrencies().map { list ->
        list.map(CurrencyEntity::toDomain)
    }

    fun observeCategories(): Flow<List<Category>> = categoryDao.observeCategories().map { list ->
        list.map(CategoryEntity::toDomain)
    }

    fun observeUserProfile(): Flow<UserProfile?> = combine(
        userDao.observeUser(),
        currencyDao.observeCurrencies()
    ) { user, currencies ->
        user ?: return@combine null
        val currency = currencies.firstOrNull { it.id == user.currencyId } ?: return@combine null
        user.toDomain(currency.toDomain())
    }

    suspend fun addAccount(
        name: String,
        type: AccountType,
        currencyId: Long,
        initialBalance: Double,
        icon: String?,
        colour: String?,
        userId: Long?
    ): Long {
        val maxPosition = accountDao.getMaxPositionForType(type.name)
        val entity = AccountEntity(
            name = name,
            type = type.name,
            currencyId = currencyId,
            initialBalance = initialBalance,
            icon = icon,
            colour = colour,
            userId = userId,
            position = maxPosition + 1
        )
        return accountDao.insert(entity)
    }

    suspend fun updateAccount(
        accountId: Long,
        name: String,
        type: AccountType,
        currencyId: Long,
        initialBalance: Double
    ) {
        val existing = accountDao.getById(accountId) ?: return
        val updated = existing.copy(
            name = name,
            type = type.name,
            currencyId = currencyId,
            initialBalance = initialBalance
        )
        accountDao.insert(updated)
    }

    suspend fun reorderAccounts(
        @Suppress("UNUSED_PARAMETER") type: AccountType,
        orderedIds: List<Long>
    ) {
        orderedIds.forEachIndexed { index, accountId ->
            accountDao.updatePosition(accountId = accountId, position = index)
        }
    }

    suspend fun deleteAccount(accountId: Long) {
        transactionDao.deleteForAccount(accountId)
        accountDao.delete(accountId)
    }

    suspend fun getAccount(accountId: Long): Account? {
        val accounts = observeAccounts().first()
        return accounts.firstOrNull { it.id == accountId }
    }

    suspend fun addTransaction(
        accountId: Long,
        categoryId: Long,
        description: String,
        amount: Double,
        type: TransactionType,
        timestamp: Instant = Instant.now()
    ): Long {
        val entity = TransactionEntity(
            amount = amount,
            date = timestamp,
            description = description,
            categoryId = categoryId,
            accountId = accountId,
            isIncome = type == TransactionType.INCOME
        )
        return transactionDao.insert(entity)
    }

    suspend fun upsertUser(name: String, currencyId: Long) {
        val existing = userDao.observeUser().first()
        if (existing == null) {
            userDao.insert(UserEntity(name = name, currencyId = currencyId))
        } else {
            userDao.update(existing.copy(name = name, currencyId = currencyId))
        }
    }

    suspend fun ensureSeedData() {
        seedCurrencies()
        seedCategories()
        seedUser()
    }

    private suspend fun seedCurrencies() {
        val existing = currencyDao.observeCurrencies().first()
        if (existing.isNotEmpty()) return
        val defaults = listOf(
            CurrencyEntity(name = "US Dollar", symbol = "\$"),
            CurrencyEntity(name = "Euro", symbol = "€"),
            CurrencyEntity(name = "British Pound", symbol = "£"),
            CurrencyEntity(name = "Japanese Yen", symbol = "¥")
        )
        defaults.forEach { currencyDao.insert(it) }
    }

    private suspend fun seedCategories() {
        val existing = categoryDao.observeCategories().first()
        if (existing.isNotEmpty()) return
        val defaults = listOf(
            CategoryEntity(name = "Salary"),
            CategoryEntity(name = "Groceries"),
            CategoryEntity(name = "Rent"),
            CategoryEntity(name = "Investments"),
            CategoryEntity(name = "Utilities")
        )
        defaults.forEach { categoryDao.insert(it) }
    }

    private suspend fun seedUser() {
        val existing = userDao.observeUser().first()
        if (existing != null) return
        val defaultCurrency = currencyDao.observeCurrencies().first().firstOrNull()?.id ?: return
        userDao.insert(UserEntity(name = "You", currencyId = defaultCurrency))
    }
}
