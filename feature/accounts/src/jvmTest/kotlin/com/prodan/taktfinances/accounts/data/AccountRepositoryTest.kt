package com.prodan.taktfinances.accounts.data

import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val created = LocalDate(2026, 11, 16)
private val nov = YearMonth(2026, 11)
private val oct = YearMonth(2026, 10)

private val salary = FinanceAccount("Salary", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), created)
private val brokerage = FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), created)
private val car = FinanceAccount("Car", Currency.MDL, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"), created)

private fun monthlyStart(minor: Long) = StartingPointPayload.Monthly(MonthlyEntry(nov, minor))
private val tradesStart = StartingPointPayload.Transactions(0)

class AccountRepositoryTest {
    private val database = inMemoryAccountsDatabase()
    private val repository = AccountRepositoryImpl(database.financeAccountDao())

    @AfterTest
    fun close() = database.close()

    private suspend fun corrupt(sql: String) = database.useWriterConnection { it.executeSQL(sql) }

    private fun <T : Any> ok(outcome: Outcome<T>): T = assertIs<Outcome.Ok<T>>(outcome).value

    @Test
    fun monthlyAccountStartsWithTheCurrentMonthEntry() = runTest {
        for (account in listOf(salary, car)) {
            val saved = ok(repository.add(account, monthlyStart(250_000)))
            val read = ok(repository.get(saved.id))
            assertEquals(saved, read)
            assertEquals(account, read.account)
            val period = assertIs<Period.Monthly>(read.period)
            assertEquals(created, period.start)
            assertEquals(listOf(MonthlyEntry(nov, 250_000)), period.entries)
        }
    }

    @Test
    fun investmentStartsWithATransactionsPeriodAtZero() = runTest {
        val saved = ok(repository.add(brokerage, tradesStart))
        assertEquals(Period.Transactions(saved.period.id, created, 0), ok(repository.get(saved.id)).period)
    }

    @Test
    fun allReturnsAccountsOldestFirstWithTheirPeriods() = runTest {
        val first = ok(repository.add(car, monthlyStart(50_000)))
        val second = ok(repository.add(brokerage, tradesStart))
        val third = ok(repository.add(salary, monthlyStart(250_000)))
        assertEquals(listOf(first, second, third), ok(repository.all()))
        assertTrue(second.id > first.id && third.id > second.id)
    }

    @Test
    fun emptyStoreGivesEmptyList() = runTest {
        assertEquals(emptyList<SavedAccount>(), ok(repository.all()))
    }

    @Test
    fun unknownIdIsUnavailable() = runTest {
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.get(42))
    }

    @Test
    fun savingAMonthInsertsThenReplaces() = runTest {
        val saved = ok(repository.add(salary, monthlyStart(250_000)))
        ok(repository.saveMonthlyEntry(saved.period.id, MonthlyEntry(oct, 200_000)))
        ok(repository.saveMonthlyEntry(saved.period.id, MonthlyEntry(nov, 310_000)))
        val entries = (ok(repository.get(saved.id)).period as Period.Monthly).entries
        assertEquals(listOf(MonthlyEntry(oct, 200_000), MonthlyEntry(nov, 310_000)), entries)
        assertEquals(entries, (ok(repository.all()).single().period as Period.Monthly).entries)
    }

    @Test
    fun deletingAMonthRemovesOnlyThatEntry() = runTest {
        val saved = ok(repository.add(salary, monthlyStart(250_000)))
        ok(repository.saveMonthlyEntry(saved.period.id, MonthlyEntry(oct, 200_000)))
        ok(repository.deleteMonthlyEntry(saved.period.id, oct))
        assertEquals(listOf(MonthlyEntry(nov, 250_000)), (ok(repository.get(saved.id)).period as Period.Monthly).entries)
    }

    @Test
    fun entriesOfOneAccountDontLeakIntoAnother() = runTest {
        val a = ok(repository.add(salary, monthlyStart(1)))
        val b = ok(repository.add(car, monthlyStart(2)))
        ok(repository.saveMonthlyEntry(a.period.id, MonthlyEntry(oct, 3)))
        val byId = ok(repository.all()).associateBy { it.id }
        assertEquals(2, (byId.getValue(a.id).period as Period.Monthly).entries.size)
        assertEquals(listOf(MonthlyEntry(nov, 2)), (byId.getValue(b.id).period as Period.Monthly).entries)
    }

    @Test
    fun unknownCodeIsUnavailable() = runTest {
        ok(repository.add(salary, monthlyStart(1)))
        corrupt("UPDATE finance_account SET bank = 'NOPE'")
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }

    @Test
    fun missingDetailIsUnavailable() = runTest {
        ok(repository.add(car, monthlyStart(1)))
        corrupt("UPDATE finance_account SET counterparty = NULL")
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }

    @Test
    fun malformedDateIsUnavailable() = runTest {
        val saved = ok(repository.add(salary, monthlyStart(1)))
        corrupt("UPDATE account_period SET start_date = 'yesterday'")
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.get(saved.id))
    }

    @Test
    fun accountWithoutPeriodIsUnavailable() = runTest {
        ok(repository.add(salary, monthlyStart(1)))
        corrupt("DELETE FROM account_period")
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }

    @Test
    fun closedStoreIsUnavailable() = runTest {
        database.close()
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.add(salary, monthlyStart(1)))
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.saveMonthlyEntry(1, MonthlyEntry(nov, 1)))
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.edit(1, "Daily", 1, MonthlyEntry(nov, 1)))
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.delete(1))
    }

    @Test
    fun editRenamesAndSetsTheMonth() = runTest {
        val saved = ok(repository.add(salary, monthlyStart(250_000)))
        ok(repository.edit(saved.id, "Daily card", saved.period.id, MonthlyEntry(nov, 300_000)))
        val read = ok(repository.get(saved.id))
        assertEquals("Daily card", read.account.name)
        assertEquals(listOf(MonthlyEntry(nov, 300_000)), assertIs<Period.Monthly>(read.period).entries)

        ok(repository.edit(saved.id, "Daily card", saved.period.id, MonthlyEntry(oct, 200_000)))
        assertEquals(
            listOf(MonthlyEntry(oct, 200_000), MonthlyEntry(nov, 300_000)),
            assertIs<Period.Monthly>(ok(repository.get(saved.id)).period).entries,
        )
    }

    @Test
    fun editWithoutEntryLeavesEntriesAlone() = runTest {
        val saved = ok(repository.add(salary, monthlyStart(250_000)))
        ok(repository.edit(saved.id, "Daily card", saved.period.id, null))
        val read = ok(repository.get(saved.id))
        assertEquals(salary.copy(name = "Daily card"), read.account)
        assertEquals(saved.period, read.period)
    }

    @Test
    fun deleteRemovesTheAccountWithItsPeriodAndEntries() = runTest {
        val kept = ok(repository.add(car, monthlyStart(50_000)))
        val saved = ok(repository.add(salary, monthlyStart(250_000)))
        ok(repository.saveMonthlyEntry(saved.period.id, MonthlyEntry(oct, 200_000)))
        ok(repository.delete(saved.id))
        assertEquals(listOf(kept), ok(repository.all()))
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.get(saved.id))
        val dao = database.financeAccountDao()
        assertEquals(null, dao.selectActivePeriod(saved.id))
        assertEquals(emptyList(), dao.selectEntries(saved.period.id))
    }
}
