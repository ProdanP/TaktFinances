package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.FormField
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.accounts.domain.entity.currencies
import com.prodan.taktfinances.accounts.domain.entity.defaultCurrency
import com.prodan.taktfinances.core.Reason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val bankDraft = AccountDetailsDraft(bank = Bank.Maib, bankAccountKind = BankAccountKind.Savings)
private val investmentDraft = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = "Binance")
private val loanDraft = AccountDetailsDraft(loanDirection = LoanDirection.Borrowed, counterparty = "Ion")

private val today = LocalDate(2026, 11, 16)

class AddAccountUseCaseTest {
    private val repository = FakeAccountRepository()
    private val add = AddAccountUseCase(repository)

    private suspend fun addAccount(
        type: AccountType,
        name: String,
        currency: Currency,
        amount: String,
        details: AccountDetailsDraft = AccountDetailsDraft(),
    ) = add(type, name, currency, amount, details, today)

    /** The starting balance the created account was stored with. */
    private fun startMinor(result: AddAccountResult): Long {
        val period = (result as AddAccountResult.Created).account.period
        return (period as Period.Monthly).entries.single().amountMinor
    }

    private fun invalid(result: AddAccountResult) = result as AddAccountResult.Invalid

    @Test
    fun validInvestmentCreatesAccountWithTrimmedPlatform() = runTest {
        val draft = investmentDraft.copy(platform = "  Binance ")
        val result = addAccount(AccountType.Investment, "  Brokerage ", Currency.EUR, "", draft)
        val expected = FinanceAccount(
            "Brokerage", Currency.EUR,
            AccountDetails.InvestmentDetails(InvestmentKind.Crypto, "Binance"),
            createdOn = today,
        )
        assertEquals(AddAccountResult.Created(SavedAccount(1, expected, Period.Transactions(1, today, 0))), result)
        assertEquals(listOf<StartingPointPayload>(StartingPointPayload.Transactions(0)), repository.starts)
        assertEquals(AccountType.Investment, expected.type)
    }

    @Test
    fun wholeAmountScalesToMinorUnits() = runTest {
        val result = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.5", bankDraft)
        assertEquals(1250, startMinor(result))
    }

    @Test
    fun bankSaveKeepsBankKindAndLast4() = runTest {
        val draft = bankDraft.copy(last4 = "0421")
        val result = addAccount(AccountType.BankAccount, "Salary", Currency.MDL, "2500", draft)
        val account = (result as AddAccountResult.Created).account.account
        assertEquals(AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), account.details)
        assertEquals(StartingPointPayload.Monthly(MonthlyEntry(YearMonth(2026, 11), 250000)), repository.starts.single())
        assertEquals(today, account.createdOn)
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
        assertTrue(repository.accounts.isEmpty())
    }

    @Test
    fun bankAndKindAreRequired() = runTest {
        val result = invalid(addAccount(AccountType.BankAccount, "Main", Currency.MDL, "1"))
        assertEquals(setOf(FormField.Bank, FormField.AccountKind), result.invalid)
        assertTrue(repository.accounts.isEmpty())
    }

    @Test
    fun blankPlatformIsInvalid() = runTest {
        val draft = investmentDraft.copy(platform = "  ")
        val result = invalid(addAccount(AccountType.Investment, "Fund", Currency.EUR, "", draft))
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
        assertEquals(50000, startMinor(borrowed))
        assertEquals(AccountDetails.LoanDetails(LoanDirection.Lent, "Ana"), l.details)
        assertEquals(20000, startMinor(lent))
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
        assertTrue(repository.accounts.isEmpty())
    }

    @Test
    fun nonNumericAmountIsInvalid() = runTest {
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "abc", loanDraft)
        assertEquals(AddAccountResult.Invalid(setOf(FormField.Amount)), result)
        assertTrue(repository.accounts.isEmpty())
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
            assertEquals(10050, startMinor(created), currency.code)
            assertTrue(addAccount(AccountType.BankAccount, "Main", currency, "100.505", bankDraft) is AddAccountResult.Invalid)
        }
    }

    @Test
    fun commaAndTrailingSeparatorAreAccepted() = runTest {
        val comma = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "2500,5", bankDraft)
        assertEquals(250050, startMinor(comma))
        val trailing = addAccount(AccountType.BankAccount, "Main", Currency.MDL, "12.", bankDraft)
        assertEquals(1200, startMinor(trailing))
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
        assertTrue(repository.accounts.isEmpty())
    }

    @Test
    fun retryAfterFailureCreatesOneAccount() = runTest {
        repository.failing = true
        addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        repository.failing = false
        val result = addAccount(AccountType.Loan, "Car", Currency.MDL, "500", loanDraft)
        assertTrue(result is AddAccountResult.Created)
        assertEquals(1, repository.accounts.size)
    }

    @Test
    fun investmentIgnoresAnyTypedAmount() = runTest {
        val result = addAccount(AccountType.Investment, "Fund", Currency.EUR, "abc", investmentDraft)
        assertEquals(Period.Transactions(1, today, 0), (result as AddAccountResult.Created).account.period)
    }

    @Test
    fun monthlyAccountStartsInTheCreationMonth() = runTest {
        val result = addAccount(AccountType.BankAccount, "Salary", Currency.MDL, "2500", bankDraft)
        val period = (result as AddAccountResult.Created).account.period as Period.Monthly
        assertEquals(today, period.start)
        assertEquals(listOf(MonthlyEntry(YearMonth(2026, 11), 250000)), period.entries)
    }
}
