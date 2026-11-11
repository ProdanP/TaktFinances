package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeRepository : AccountRepository {
    val added = mutableListOf<FinanceAccount>()
    override fun add(account: FinanceAccount) { added += account }
    override fun all(): List<FinanceAccount> = added
}

private val bankDraft = AccountDetailsDraft(bank = Bank.Maib, bankAccountKind = BankAccountKind.Savings)
private val investmentDraft = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = "Binance")
private val loanDraft = AccountDetailsDraft(loanDirection = LoanDirection.Borrowed, counterparty = "Ion")

class AddAccountTest {
    private val repository = FakeRepository()
    private val addAccount = AddAccount(repository)

    private fun invalid(result: AddAccountResult) = result as AddAccountResult.Invalid

    @Test
    fun validInvestmentCreatesAccountWithTrimmedPlatform() {
        val draft = investmentDraft.copy(platform = "  Binance ")
        val result = addAccount(AccountType.Investment, "  Brokerage ", Currency.EUR, "1500.50", draft)
        val expected = FinanceAccount(
            "Brokerage", Currency.EUR, 150050,
            AccountDetails.InvestmentDetails(InvestmentKind.Crypto, "Binance"),
        )
        assertEquals(AddAccountResult.Created(expected), result)
        assertEquals(listOf(expected), repository.added)
        assertEquals(AccountType.Investment, expected.type)
    }

    @Test
    fun wholeAmountScalesToMinorUnits() {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.5", bankDraft)
        assertEquals(1250, (result as AddAccountResult.Created).account.amountMinor)
    }

    @Test
    fun bankSaveKeepsBankKindAndLast4() {
        val draft = bankDraft.copy(last4 = "0421")
        val result = addAccount(AccountType.BankAccount, "Salary", Currency.MDL, "2500", draft)
        val account = (result as AddAccountResult.Created).account
        assertEquals(AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), account.details)
        assertEquals(250000, account.amountMinor)
    }

    @Test
    fun omittedLast4IsNull() {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1", bankDraft)
        val details = (result as AddAccountResult.Created).account.details as AccountDetails.BankDetails
        assertEquals(null, details.last4)
    }

    @Test
    fun badLast4IsInvalid() {
        for (value in listOf("12", "12a4", "123", "12345", "1234567890123456")) {
            val result = invalid(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1", bankDraft.copy(last4 = value)))
            assertEquals(setOf(FormField.Last4), result.invalid, value)
        }
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun bankAndKindAreRequired() {
        val result = invalid(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1"))
        assertEquals(setOf(FormField.Bank, FormField.AccountKind), result.invalid)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun blankPlatformIsInvalid() {
        val draft = investmentDraft.copy(platform = "  ")
        val result = invalid(addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", draft))
        assertEquals(setOf(FormField.Platform), result.invalid)
    }

    @Test
    fun investmentKindIsRequired() {
        val result = invalid(addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", investmentDraft.copy(investmentKind = null)))
        assertEquals(setOf(FormField.InvestmentKind), result.invalid)
    }

    @Test
    fun borrowedAndLentLoansKeepPositiveAmount() {
        val borrowed = addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        val lent = addAccount(AccountType.Loan, "Help", Currency.MDL, "200", loanDraft.copy(loanDirection = LoanDirection.Lent, counterparty = " Ana "))
        val b = (borrowed as AddAccountResult.Created).account
        val l = (lent as AddAccountResult.Created).account
        assertEquals(AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"), b.details)
        assertEquals(50000, b.amountMinor)
        assertEquals(AccountDetails.LoanDetails(LoanDirection.Lent, "Ana"), l.details)
        assertEquals(20000, l.amountMinor)
    }

    @Test
    fun blankCounterpartyAndMissingDirectionAreInvalid() {
        val result = invalid(addAccount(AccountType.Loan, "Car", Currency.MDL, "1", AccountDetailsDraft(counterparty = " ")))
        assertEquals(setOf(FormField.Direction, FormField.Counterparty), result.invalid)
    }

    @Test
    fun severalProblemsAreReportedTogether() {
        val result = invalid(addAccount(AccountType.Loan, "  ", Currency.MDL, "abc", AccountDetailsDraft()))
        assertEquals(
            setOf(FormField.Name, FormField.Amount, FormField.Direction, FormField.Counterparty),
            result.invalid,
        )
    }

    @Test
    fun detailsOfOtherTypesAreIgnored() {
        val everything = bankDraft.copy(
            last4 = "bad", investmentKind = InvestmentKind.Bonds, platform = "X",
            loanDirection = LoanDirection.Lent, counterparty = "Y",
        )
        val result = addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", everything)
        val account = (result as AddAccountResult.Created).account
        assertEquals(AccountDetails.InvestmentDetails(InvestmentKind.Bonds, "X"), account.details)
    }

    @Test
    fun blankNameIsInvalid() {
        val result = addAccount(AccountType.Loan, "   ", Currency.MDL, "10", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Name)), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun nonNumericAmountIsInvalid() {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "abc", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun negativeAmountIsInvalid() {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "-100", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
    }

    @Test
    fun overPreciseAmountIsInvalid() {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.USD, "1.234", bankDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
    }

    @Test
    fun everyCurrencyUsesTwoDecimals() {
        for (currency in currencies()) {
            val created = addAccount(AccountType.BankAccount, "Main", currency, "100.5", bankDraft)
            assertEquals(10050, (created as AddAccountResult.Created).account.amountMinor, currency.code)
            assertTrue(addAccount(AccountType.BankAccount, "Main", currency, "100.505", bankDraft) is AddAccountResult.Invalid)
        }
    }

    @Test
    fun commaAndTrailingSeparatorAreAccepted() {
        val comma = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "2500,5", bankDraft)
        assertEquals(250050, (comma as AddAccountResult.Created).account.amountMinor)
        val trailing = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.", bankDraft)
        assertEquals(1200, (trailing as AddAccountResult.Created).account.amountMinor)
    }

    @Test
    fun emptyAmountAndOverflowAreInvalid() {
        assertTrue(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "", bankDraft) is AddAccountResult.Invalid)
        assertTrue(
            addAccount(AccountType.BankAccount, "Main", Currency.MDL, "99999999999999999999", bankDraft) is AddAccountResult.Invalid,
        )
    }

    @Test
    fun defaultCurrencyIsMdl() {
        assertEquals(Currency.MDL, defaultCurrency())
    }
}
