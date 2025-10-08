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
import com.andydel.financemanager.data.local.models.AccountWithTransactions
import com.andydel.financemanager.data.remote.ExchangeRateService
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val currencyDao: CurrencyDao,
    private val userDao: UserDao,
    private val exchangeRateService: ExchangeRateService
) {

    fun observeAccounts(): Flow<List<Account>> = accountsWithCurrencies()
        .map { (accounts, currencies) ->
            accounts.toDomainAccounts(currencies)
        }

    fun observeAccountsOverview(): Flow<AccountsOverview> = combine(
        accountsWithCurrencies(),
        userDao.observeUser()
    ) { (accounts, currencies), user ->
        val domainAccounts = accounts.toDomainAccounts(currencies)
        val baseCurrency = user?.currencyId
            ?.let(currencies::get)
            ?.toDomain()
        val baseCurrencyAmounts = if (baseCurrency != null) {
            computeBaseAmounts(domainAccounts, baseCurrency)
        } else {
            domainAccounts.associate { it.id to it.currentBalance }
        }
        AccountsOverview(
            accounts = domainAccounts,
            baseCurrency = baseCurrency,
            baseCurrencyAmounts = baseCurrencyAmounts
        )
    }

    fun observeAccounts(type: AccountType): Flow<List<Account>> = observeAccounts().map { list ->
        list.filter { it.type == type }
    }

    fun observeTransactions(accountId: Long): Flow<List<FinanceTransaction>> =
        transactionDao.observeForAccount(accountId).map { entities ->
            entities.map(TransactionEntity::toDomain)
        }

    fun observeSummary(): Flow<SummarySnapshot> = observeAccountsOverview().map { overview ->
        val accounts = overview.accounts
        val amounts = overview.baseCurrencyAmounts
        fun totalFor(type: AccountType) = accounts
            .filter { it.type == type }
            .sumOf { account -> amounts[account.id] ?: account.currentBalance }
        val current = totalFor(AccountType.CURRENT)
        val savings = totalFor(AccountType.SAVINGS)
        val debt = totalFor(AccountType.DEBT)
        SummarySnapshot(
            currentBalance = current,
            savingsBalance = savings,
            debtBalance = debt,
            totalAssets = current + savings,
            totalDebt = debt,
            baseCurrency = overview.baseCurrency
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

    suspend fun updateTransaction(
        transactionId: Long,
        accountId: Long,
        categoryId: Long,
        description: String,
        amount: Double,
        type: TransactionType,
        timestamp: Instant
    ) {
        val entity = TransactionEntity(
            id = transactionId,
            amount = amount,
            date = timestamp,
            description = description,
            categoryId = categoryId,
            accountId = accountId,
            isIncome = type == TransactionType.INCOME
        )
        transactionDao.insert(entity)
    }

    suspend fun getTransaction(transactionId: Long): FinanceTransaction? {
        return transactionDao.getById(transactionId)?.toDomain()
    }

    suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteById(transactionId)
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
            CurrencyEntity(name = "US Dollar", symbol = "\$", code = "USD"),
            CurrencyEntity(name = "Euro", symbol = "€", code = "EUR"),
            CurrencyEntity(name = "British Pound", symbol = "£", code = "GBP"),
            CurrencyEntity(name = "Japanese Yen", symbol = "¥", code = "JPY")
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
    private fun accountsWithCurrencies(): Flow<Pair<List<AccountWithTransactions>, Map<Long, CurrencyEntity>>> =
        combine(
            accountDao.observeAccounts(),
            currencyDao.observeCurrencies()
        ) { accounts, currencies ->
            accounts to currencies.associateBy(CurrencyEntity::id)
        }

    private suspend fun computeBaseAmounts(
        accounts: List<Account>,
        baseCurrency: Currency
    ): Map<Long, Double> {
        if (accounts.isEmpty()) return emptyMap()
        val foreignAccounts = accounts.filter { it.currency.id != baseCurrency.id }
        if (foreignAccounts.isEmpty()) {
            return accounts.associate { it.id to it.currentBalance }
        }
        val targetSymbols = foreignAccounts.map { it.currency.code.uppercase() }.toSet()
        val rates = exchangeRateService.latestRates(baseCurrency.code.uppercase(), targetSymbols)
        return convertAccountBalancesToBase(accounts, baseCurrency, rates)
    }
}

data class AccountsOverview(
    val accounts: List<Account>,
    val baseCurrency: Currency?,
    val baseCurrencyAmounts: Map<Long, Double>
)

private fun List<AccountWithTransactions>.toDomainAccounts(
    currencies: Map<Long, CurrencyEntity>
): List<Account> = mapNotNull { accountWithTransactions ->
    val currencyEntity = currencies[accountWithTransactions.account.currencyId] ?: return@mapNotNull null
    accountWithTransactions.account.toDomain(
        currency = currencyEntity.toDomain(),
        transactions = accountWithTransactions.transactions
    )
}.sortedBy(Account::sortOrder)

internal fun convertAccountBalancesToBase(
    accounts: List<Account>,
    baseCurrency: Currency,
    rates: Map<String, Double>
): Map<Long, Double> {
    if (accounts.isEmpty()) return emptyMap()
    return accounts.associate { account ->
        val amount = if (account.currency.id == baseCurrency.id) {
            account.currentBalance
        } else {
            val rate = rates[account.currency.code.uppercase()]
            if (rate != null && rate > 0.0) account.currentBalance / rate else account.currentBalance
        }
        account.id to amount
    }
}
