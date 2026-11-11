package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeRepository : AccountRepository {
    val added = mutableListOf<FinanceAccount>()
    override fun add(account: FinanceAccount) { added += account }
    override fun all(): List<FinanceAccount> = added
}

class AddAccountTest {
    private val repository = FakeRepository()
    private val addAccount = AddAccount(repository)

    @Test
    fun validInputCreatesAccount() {
        val result = addAccount(AccountType.Investment, "  Brokerage ", Currency.EUR, "1500.50")
        val expected = FinanceAccount(AccountType.Investment, "Brokerage", Currency.EUR, 150050)
        assertEquals(AddAccountResult.Created(expected), result)
        assertEquals(listOf(expected), repository.added)
    }

    @Test
    fun wholeAmountScalesToMinorUnits() {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.5")
        assertEquals(1250, (result as AddAccountResult.Created).account.amountMinor)
    }

    @Test
    fun blankNameIsInvalid() {
        val result = addAccount(AccountType.Loan, "   ", Currency.MDL, "10")
        assertEquals(AddAccountResult.Invalid(nameInvalid = true, amountInvalid = false), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun nonNumericAmountIsInvalid() {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "abc")
        assertEquals(AddAccountResult.Invalid(nameInvalid = false, amountInvalid = true), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun negativeAmountIsInvalid() {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "-5")
        assertEquals(AddAccountResult.Invalid(nameInvalid = false, amountInvalid = true), result)
    }

    @Test
    fun overPreciseAmountIsInvalid() {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.USD, "1.234")
        assertEquals(AddAccountResult.Invalid(nameInvalid = false, amountInvalid = true), result)
    }

    @Test
    fun jpyRejectsDecimalsButAcceptsWhole() {
        val rejected = addAccount(AccountType.BankAccount, "Main", Currency.JPY, "100.5")
        assertTrue(rejected is AddAccountResult.Invalid)
        val created = addAccount(AccountType.BankAccount, "Main", Currency.JPY, "100")
        assertEquals(100, (created as AddAccountResult.Created).account.amountMinor)
    }

    @Test
    fun emptyAmountAndOverflowAreInvalid() {
        assertTrue(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "") is AddAccountResult.Invalid)
        assertTrue(
            addAccount(AccountType.BankAccount, "Main", Currency.MDL, "99999999999999999999") is AddAccountResult.Invalid,
        )
    }

    @Test
    fun defaultCurrencyIsMdl() {
        assertEquals(Currency.MDL, defaultCurrency())
    }
}
