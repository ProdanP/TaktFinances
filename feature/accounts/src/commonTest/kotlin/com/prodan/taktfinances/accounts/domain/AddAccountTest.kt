package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeRepository : AccountRepository {
    val added = mutableListOf<FinanceAccount>()
    var failing = false
    override suspend fun add(account: FinanceAccount): Outcome<SavedAccount> {
        if (failing) return Outcome.Failed(Reason.Unavailable)
        added += account
        return Outcome.Ok(SavedAccount(added.size.toLong(), account))
    }
    override suspend fun all(): Outcome<List<SavedAccount>> =
        Outcome.Ok(added.mapIndexed { index, account -> SavedAccount(index + 1L, account) })
}

private val bankDraft = AccountDetailsDraft(bank = Bank.Maib, bankAccountKind = BankAccountKind.Savings)
private val investmentDraft = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = "Binance")
private val loanDraft = AccountDetailsDraft(loanDirection = LoanDirection.Borrowed, counterparty = "Ion")

class AddAccountTest {
    private val repository = FakeRepository()
    private val addAccount = AddAccount(repository)

    private fun invalid(result: AddAccountResult) = result as AddAccountResult.Invalid

    @Test
    fun validInvestmentCreatesAccountWithTrimmedPlatform() = runTest {
        val draft = investmentDraft.copy(platform = "  Binance ")
        val result = addAccount(AccountType.Investment, "  Brokerage ", Currency.EUR, "1500.50", draft)
        val expected = FinanceAccount(
            "Brokerage", Currency.EUR, 150050,
            AccountDetails.InvestmentDetails(InvestmentKind.Crypto, "Binance"),
        )
        assertEquals(AddAccountResult.Created(SavedAccount(1, expected)), result)
        assertEquals(listOf(expected), repository.added)
        assertEquals(AccountType.Investment, expected.type)
    }

    @Test
    fun wholeAmountScalesToMinorUnits() = runTest {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.5", bankDraft)
        assertEquals(1250, (result as AddAccountResult.Created).account.account.amountMinor)
    }

    @Test
    fun bankSaveKeepsBankKindAndLast4() = runTest {
        val draft = bankDraft.copy(last4 = "0421")
        val result = addAccount(AccountType.BankAccount, "Salary", Currency.MDL, "2500", draft)
        val account = (result as AddAccountResult.Created).account.account
        assertEquals(AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), account.details)
        assertEquals(250000, account.amountMinor)
    }

    @Test
    fun omittedLast4IsNull() = runTest {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1", bankDraft)
        val details = (result as AddAccountResult.Created).account.account.details as AccountDetails.BankDetails
        assertEquals(null, details.last4)
    }

    @Test
    fun badLast4IsInvalid() = runTest {
        for (value in listOf("12", "12a4", "123", "12345", "1234567890123456")) {
            val result = invalid(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1", bankDraft.copy(last4 = value)))
            assertEquals(setOf(FormField.Last4), result.invalid, value)
        }
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun bankAndKindAreRequired() = runTest {
        val result = invalid(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1"))
        assertEquals(setOf(FormField.Bank, FormField.AccountKind), result.invalid)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun blankPlatformIsInvalid() = runTest {
        val draft = investmentDraft.copy(platform = "  ")
        val result = invalid(addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", draft))
        assertEquals(setOf(FormField.Platform), result.invalid)
    }

    @Test
    fun investmentKindIsRequired() = runTest {
        val result = invalid(addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", investmentDraft.copy(investmentKind = null)))
        assertEquals(setOf(FormField.InvestmentKind), result.invalid)
    }

    @Test
    fun borrowedAndLentLoansKeepPositiveAmount() = runTest {
        val borrowed = addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        val lent = addAccount(AccountType.Loan, "Help", Currency.MDL, "200", loanDraft.copy(loanDirection = LoanDirection.Lent, counterparty = " Ana "))
        val b = (borrowed as AddAccountResult.Created).account.account
        val l = (lent as AddAccountResult.Created).account.account
        assertEquals(AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"), b.details)
        assertEquals(50000, b.amountMinor)
        assertEquals(AccountDetails.LoanDetails(LoanDirection.Lent, "Ana"), l.details)
        assertEquals(20000, l.amountMinor)
    }

    @Test
    fun blankCounterpartyAndMissingDirectionAreInvalid() = runTest {
        val result = invalid(addAccount(AccountType.Loan, "Car", Currency.MDL, "1", AccountDetailsDraft(counterparty = " ")))
        assertEquals(setOf(FormField.Direction, FormField.Counterparty), result.invalid)
    }

    @Test
    fun severalProblemsAreReportedTogether() = runTest {
        val result = invalid(addAccount(AccountType.Loan, "  ", Currency.MDL, "abc", AccountDetailsDraft()))
        assertEquals(
            setOf(FormField.Name, FormField.Amount, FormField.Direction, FormField.Counterparty),
            result.invalid,
        )
    }

    @Test
    fun detailsOfOtherTypesAreIgnored() = runTest {
        val everything = bankDraft.copy(
            last4 = "bad", investmentKind = InvestmentKind.Bonds, platform = "X",
            loanDirection = LoanDirection.Lent, counterparty = "Y",
        )
        val result = addAccount(AccountType.Investment, "Fund", Currency.EUR, "1", everything)
        val account = (result as AddAccountResult.Created).account.account
        assertEquals(AccountDetails.InvestmentDetails(InvestmentKind.Bonds, "X"), account.details)
    }

    @Test
    fun blankNameIsInvalid() = runTest {
        val result = addAccount(AccountType.Loan, "   ", Currency.MDL, "10", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Name)), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun nonNumericAmountIsInvalid() = runTest {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "abc", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun negativeAmountIsInvalid() = runTest {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "-100", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
    }

    @Test
    fun overPreciseAmountIsInvalid() = runTest {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.USD, "1.234", bankDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
    }

    @Test
    fun everyCurrencyUsesTwoDecimals() = runTest {
        for (currency in currencies()) {
            val created = addAccount(AccountType.BankAccount, "Main", currency, "100.5", bankDraft)
            assertEquals(10050, (created as AddAccountResult.Created).account.account.amountMinor, currency.code)
            assertTrue(addAccount(AccountType.BankAccount, "Main", currency, "100.505", bankDraft) is AddAccountResult.Invalid)
        }
    }

    @Test
    fun commaAndTrailingSeparatorAreAccepted() = runTest {
        val comma = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "2500,5", bankDraft)
        assertEquals(250050, (comma as AddAccountResult.Created).account.account.amountMinor)
        val trailing = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.", bankDraft)
        assertEquals(1200, (trailing as AddAccountResult.Created).account.account.amountMinor)
    }

    @Test
    fun emptyAmountAndOverflowAreInvalid() = runTest {
        assertTrue(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "", bankDraft) is AddAccountResult.Invalid)
        assertTrue(
            addAccount(AccountType.BankAccount, "Main", Currency.MDL, "99999999999999999999", bankDraft) is AddAccountResult.Invalid,
        )
    }

    @Test
    fun defaultCurrencyIsMdl() = runTest {
        assertEquals(Currency.MDL, defaultCurrency())
    }

    @Test
    fun storageFailureCreatesNothing() = runTest {
        repository.failing = true
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        assertEquals(AddAccountResult.Failed(Reason.Unavailable), result)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun retryAfterFailureCreatesOneAccount() = runTest {
        repository.failing = true
        addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        repository.failing = false
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        assertTrue(result is AddAccountResult.Created)
        assertEquals(1, repository.added.size)
    }
}
