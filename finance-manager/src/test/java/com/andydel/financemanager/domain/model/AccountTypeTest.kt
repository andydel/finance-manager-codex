package com.andydel.financemanager.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountTypeTest {

    @Test
    fun `current account income increases balance`() {
        val change = AccountType.CURRENT.balanceImpact(isIncome = true, amount = 120.0)
        assertEquals(120.0, change, 0.0)
    }

    @Test
    fun `current account expense reduces balance`() {
        val change = AccountType.CURRENT.balanceImpact(isIncome = false, amount = 75.5)
        assertEquals(-75.5, change, 0.0)
    }

    @Test
    fun `debt account income reduces balance`() {
        val change = AccountType.DEBT.balanceImpact(isIncome = true, amount = 200.0)
        assertEquals(-200.0, change, 0.0)
    }

    @Test
    fun `debt account expense increases balance`() {
        val change = AccountType.DEBT.balanceImpact(isIncome = false, amount = 60.25)
        assertEquals(60.25, change, 0.0)
    }

    @Test
    fun `fromRaw matches display name`() {
        val type = AccountType.fromRaw("Savings & Investments")
        assertEquals(AccountType.SAVINGS, type)
    }

    @Test
    fun `fromRaw handles noisy alias`() {
        val type = AccountType.fromRaw("  Checking  ")
        assertEquals(AccountType.CURRENT, type)
    }

    @Test
    fun `fromRaw defaults to current when unknown`() {
        val type = AccountType.fromRaw("mystery")
        assertEquals(AccountType.CURRENT, type)
    }
}
