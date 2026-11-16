package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.core.Reason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val created = LocalDate(2026, 9, 20)
private val today = LocalDate(2026, 11, 16)
private val sep = YearMonth(2026, 9)
private val oct = YearMonth(2026, 10)
private val nov = YearMonth(2026, 11)

private val salary = SavedAccount(
    7,
    FinanceAccount("Salary", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, null), created),
    Period.Monthly(3, created, listOf(MonthlyEntry(sep, 900_000), MonthlyEntry(oct, 1_000_000))),
)

private val brokerage = SavedAccount(
    8,
    FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), created),
    Period.Transactions(4, created, 0),
)

class CheckInUseCaseTest {
    private val repository = FakeAccountRepository()
    private val checkIn = CheckInUseCase(repository)

    @Test
    fun validCheckInIsStoredOnce() = runTest {
        assertEquals(CheckInResult.Saved, checkIn(salary, nov, "11500,5", today))
        assertEquals(listOf(3L to MonthlyEntry(nov, 1_150_050)), repository.savedEntries)
    }

    @Test
    fun existingMonthIsReplaced() = runTest {
        assertEquals(CheckInResult.Saved, checkIn(salary, oct, "10500", today))
        assertEquals(listOf(3L to MonthlyEntry(oct, 1_050_000)), repository.savedEntries)
    }

    @Test
    fun invalidInputNeverReachesTheRepository() = runTest {
        assertEquals(CheckInResult.Invalid, checkIn(salary, YearMonth(2026, 12), "1", today))
        assertEquals(CheckInResult.Invalid, checkIn(salary, YearMonth(2026, 8), "1", today))
        assertEquals(CheckInResult.Invalid, checkIn(salary, nov, "", today))
        assertEquals(CheckInResult.Invalid, checkIn(salary, nov, "abc", today))
        assertEquals(CheckInResult.Invalid, checkIn(brokerage, nov, "1", today))
        assertTrue(repository.savedEntries.isEmpty())
    }

    @Test
    fun storageFailureIsReported() = runTest {
        repository.failing = true
        assertEquals(CheckInResult.Failed(Reason.Unavailable), checkIn(salary, nov, "1", today))
    }
}

class DeleteCheckInTest {
    private val repository = FakeAccountRepository()
    private val delete = DeleteCheckInUseCase(repository)

    @Test
    fun deletesALaterEntry() = runTest {
        assertEquals(DeleteCheckInResult.Deleted, delete(salary, oct))
        assertEquals(listOf(3L to oct), repository.deletedMonths)
    }

    @Test
    fun startMonthMissingMonthAndInvestmentAreRefused() = runTest {
        assertEquals(DeleteCheckInResult.Invalid, delete(salary, sep))
        assertEquals(DeleteCheckInResult.Invalid, delete(salary, nov))
        assertEquals(DeleteCheckInResult.Invalid, delete(brokerage, nov))
        assertTrue(repository.deletedMonths.isEmpty())
    }

    @Test
    fun storageFailureIsReported() = runTest {
        repository.failing = true
        assertEquals(DeleteCheckInResult.Failed(Reason.Unavailable), delete(salary, oct))
    }
}
