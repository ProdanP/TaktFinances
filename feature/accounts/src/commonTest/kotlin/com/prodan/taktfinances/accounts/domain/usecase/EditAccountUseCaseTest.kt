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

/** November is not checked in: it carries October's 2,500.00 forward. */
private val salary = SavedAccount(
    7,
    FinanceAccount("Salary", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, null), created),
    Period.Monthly(3, created, listOf(MonthlyEntry(sep, 200_000), MonthlyEntry(oct, 250_000))),
)

private val brokerage = SavedAccount(
    8,
    FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), created),
    Period.Transactions(4, created, 0),
)

class EditAccountUseCaseTest {
    private val repository = FakeAccountRepository()
    private val edit = EditAccountUseCase(repository)

    @Test
    fun blankNameIsInvalid() = runTest {
        assertEquals(EditAccountResult.Invalid, edit(salary, "   ", "2500", today))
        assertTrue(repository.edits.isEmpty())
    }

    @Test
    fun renameOnlyWritesNoEntry() = runTest {
        assertEquals(EditAccountResult.Saved, edit(salary, "  Daily card ", "2500.00", today))
        assertEquals(listOf(FakeAccountRepository.Edit(7, "Daily card", 3, null)), repository.edits)
    }

    @Test
    fun differentAmountSetsTheCurrentMonth() = runTest {
        assertEquals(EditAccountResult.Saved, edit(salary, "Salary", "2900", today))
        assertEquals(listOf(FakeAccountRepository.Edit(7, "Salary", 3, MonthlyEntry(nov, 290_000))), repository.edits)
    }

    @Test
    fun nothingChangedStoresNothing() = runTest {
        assertEquals(EditAccountResult.Unchanged, edit(salary, "Salary", "2500", today))
        assertTrue(repository.edits.isEmpty())
    }

    @Test
    fun amountEqualToTheCurrentEntryWritesNoEntry() = runTest {
        val checkedIn = salary.copy(period = Period.Monthly(3, created, listOf(MonthlyEntry(sep, 200_000), MonthlyEntry(nov, 300_000))))
        assertEquals(EditAccountResult.Saved, edit(checkedIn, "Daily", "3000", today))
        assertEquals(null, repository.edits.single().entry)
    }

    @Test
    fun monthlyWithoutAmountIsInvalid() = runTest {
        assertEquals(EditAccountResult.Invalid, edit(salary, "Daily", "", today))
        assertTrue(repository.edits.isEmpty())
    }

    @Test
    fun investmentIgnoresTheAmount() = runTest {
        assertEquals(EditAccountResult.Unchanged, edit(brokerage, "Brokerage", "", today))
        assertEquals(EditAccountResult.Saved, edit(brokerage, "XTB", "999", today))
        assertEquals(listOf(FakeAccountRepository.Edit(8, "XTB", 4, null)), repository.edits)
    }

    @Test
    fun storageFailureIsReported() = runTest {
        repository.failing = true
        assertEquals(EditAccountResult.Failed(Reason.Unavailable), edit(salary, "Daily", "2500", today))
    }
}
