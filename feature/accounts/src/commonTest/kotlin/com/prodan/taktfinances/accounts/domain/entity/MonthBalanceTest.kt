package com.prodan.taktfinances.accounts.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val bank = AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, null)

private fun monthly(created: LocalDate, vararg entries: Pair<YearMonth, Long>) = SavedAccount(
    1,
    FinanceAccount("Salary", Currency.MDL, bank, created),
    Period.Monthly(1, created, entries.map { MonthlyEntry(it.first, it.second) }),
)

private val sep = YearMonth(2026, 9)
private val oct = YearMonth(2026, 10)
private val nov = YearMonth(2026, 11)

class MonthBalanceTest {
    @Test
    fun monthWithAnEntryUsesIt() {
        val account = monthly(LocalDate(2026, 10, 3), oct to 1_000_000, nov to 1_150_000)
        val balances = monthEndBalances(account, LocalDate(2026, 11, 16))
        assertEquals(listOf(MonthBalance(oct, 1_000_000, true), MonthBalance(nov, 1_150_000, true)), balances)
    }

    @Test
    fun missingMonthCarriesTheEarlierEntryForward() {
        val account = monthly(LocalDate(2026, 9, 1), sep to 900_000, nov to 1_150_000)
        val october = monthEndBalances(account, LocalDate(2026, 11, 16))[1]
        assertEquals(MonthBalance(oct, 900_000, checkedIn = false), october)
    }

    @Test
    fun currentMonthWithoutEntryIsNeverZero() {
        val account = monthly(LocalDate(2026, 10, 1), oct to 1_000_000)
        val today = LocalDate(2026, 11, 2)
        assertEquals(MonthBalance(nov, 1_000_000, checkedIn = false), monthEndBalances(account, today).last())
        assertEquals(1_000_000, account.balanceMinor(today))
    }

    @Test
    fun monthsBeforeTheAccountAreNotIncluded() {
        val account = monthly(LocalDate(2026, 11, 10), nov to 250_000)
        assertEquals(listOf(nov), monthEndBalances(account, LocalDate(2026, 11, 16)).map { it.month })
    }

    @Test
    fun balancesRunAcrossYears() {
        val account = monthly(LocalDate(2025, 12, 1), YearMonth(2025, 12) to 100)
        val months = monthEndBalances(account, LocalDate(2026, 2, 1)).map { it.month }
        assertEquals(listOf(YearMonth(2025, 12), YearMonth(2026, 1), YearMonth(2026, 2)), months)
    }

    @Test
    fun clockBeforeCreationStillGivesTheStartMonth() {
        val account = monthly(LocalDate(2026, 11, 10), nov to 250_000)
        assertEquals(250_000, account.balanceMinor(LocalDate(2026, 10, 1)))
    }

    @Test
    fun investmentStaysFlatAtZero() {
        val account = SavedAccount(
            2,
            FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), LocalDate(2026, 10, 5)),
            Period.Transactions(2, LocalDate(2026, 10, 5), 0),
        )
        val balances = monthEndBalances(account, LocalDate(2026, 11, 16))
        assertEquals(listOf(MonthBalance(oct, 0, true), MonthBalance(nov, 0, true)), balances)
        assertEquals(0, account.balanceMinor(LocalDate(2026, 11, 16)))
    }

    @Test
    fun changeHasAmountAndPercent() {
        assertEquals(MonthChange(150_000, 150), monthChange(1_000_000, 1_150_000))
        assertEquals(MonthChange(-50_000, -50), monthChange(1_000_000, 950_000))
        assertEquals(MonthChange(0, 0), monthChange(1_000_000, 1_000_000))
        assertEquals(MonthChange(153_000, 142), monthChange(1_081_000, 1_234_000))
    }

    @Test
    fun zeroPreviousHidesPercentAndFirstMonthHasNoChange() {
        assertEquals(MonthChange(50_000, null), monthChange(0, 50_000))
        assertNull(monthChange(null, 50_000))
    }

    @Test
    fun changesFollowBalances() {
        val balances = listOf(MonthBalance(sep, 900_000, true), MonthBalance(oct, 1_000_000, true))
        assertEquals(listOf(null, MonthChange(100_000, 111)), monthChanges(balances))
    }

    @Test
    fun checkInMonthsRunFromNowBackToTheStart() {
        val account = monthly(LocalDate(2026, 9, 20), sep to 1)
        val period = account.period as Period.Monthly
        assertEquals(listOf(nov, oct, sep), checkInMonths(period, LocalDate(2026, 11, 16)))
    }

    @Test
    fun accountCreatedThisMonthOffersOnlyThisMonth() {
        val period = monthly(LocalDate(2026, 11, 1), nov to 1).period as Period.Monthly
        assertEquals(listOf(nov), checkInMonths(period, LocalDate(2026, 11, 30)))
    }

    @Test
    fun entryRulesForTheTimelineAndRecentActivity() {
        val period = monthly(LocalDate(2026, 9, 1), sep to 1, oct to 2).period as Period.Monthly
        assertFalse(period.canDeleteEntry(sep))
        assertTrue(period.canDeleteEntry(oct))
        assertFalse(period.canDeleteEntry(nov))
        assertTrue(period.hasCheckIns)
        assertEquals(listOf(oct, sep), period.recentEntries().map { it.month })
        assertFalse((monthly(LocalDate(2026, 9, 1), sep to 1).period as Period.Monthly).hasCheckIns)
    }

    @Test
    fun recentEntriesKeepTheFiveNewest() {
        val entries = (1..8).map { YearMonth(2026, it) to it.toLong() }.toTypedArray()
        val period = monthly(LocalDate(2026, 1, 1), *entries).period as Period.Monthly
        assertEquals((8 downTo 4).map { YearMonth(2026, it) }, period.recentEntries().map { it.month })
    }
}
