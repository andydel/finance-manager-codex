package com.andydel.financemanager.data.repository

import com.andydel.financemanager.domain.model.Account
import com.andydel.financemanager.domain.model.AccountType
import com.andydel.financemanager.domain.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertAccountBalancesToBaseTest {

    private val usd = Currency(id = 1L, name = "US Dollar", symbol = "\$", code = "USD")
    private val eur = Currency(id = 2L, name = "Euro", symbol = "€", code = "EUR")
    private val gbp = Currency(id = 3L, name = "British Pound", symbol = "£", code = "GBP")

    @Test
    fun `returns original balances when account already in base currency`() {
        val accounts = listOf(account(id = 1L, currency = usd, balance = 120.0))

        val converted = convertAccountBalancesToBase(accounts, usd, emptyMap())

        assertEquals(120.0, converted.getValue(1L), 0.0001)
    }

    @Test
    fun `converts foreign balances using supplied rates`() {
        val accounts = listOf(account(id = 2L, currency = eur, balance = 80.0))
        val rates = mapOf("EUR" to 0.8)

        val converted = convertAccountBalancesToBase(accounts, usd, rates)

        assertEquals(100.0, converted.getValue(2L), 0.0001)
    }

    @Test
    fun `falls back to original balance when rate missing`() {
        val accounts = listOf(account(id = 3L, currency = gbp, balance = 150.0))
        val rates = emptyMap<String, Double>()

        val converted = convertAccountBalancesToBase(accounts, usd, rates)

        assertEquals(150.0, converted.getValue(3L), 0.0001)
    }

    private fun account(
        id: Long,
        currency: Currency,
        balance: Double,
        type: AccountType = AccountType.CURRENT
    ): Account = Account(
        id = id,
        name = "Account $id",
        type = type,
        currency = currency,
        initialBalance = balance,
        currentBalance = balance,
        icon = null,
        color = null,
        sortOrder = id.toInt()
    )
}
