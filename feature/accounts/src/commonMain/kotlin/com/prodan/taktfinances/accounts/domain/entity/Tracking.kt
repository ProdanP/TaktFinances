package com.prodan.taktfinances.accounts.domain.entity

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/** How an account's balance is kept up to date. Investments always use [Transactions] (trades). */
enum class TrackingMode { Monthly, Transactions }

/** The balance of a Monthly account for one calendar month. */
data class MonthlyEntry(val month: YearMonth, val amountMinor: Long)

/** The account's active tracking period. */
sealed interface Period {
    val id: Long

    /** The day the period started; its month is the first month it covers. */
    val start: LocalDate

    val mode: TrackingMode

    val startMonth: YearMonth get() = start.yearMonth

    /** [entries] are sorted by month; the first one is the start month's entry (the starting balance). */
    data class Monthly(
        override val id: Long,
        override val start: LocalDate,
        val entries: List<MonthlyEntry>,
    ) : Period {
        override val mode: TrackingMode get() = TrackingMode.Monthly
    }

    /** No transactions or trades exist yet, so the balance is [openingBalanceMinor] throughout. */
    data class Transactions(
        override val id: Long,
        override val start: LocalDate,
        val openingBalanceMinor: Long,
    ) : Period {
        override val mode: TrackingMode get() = TrackingMode.Transactions
    }
}

/** The entry for [month], or null when that month was not checked in. */
fun Period.Monthly.entryFor(month: YearMonth): MonthlyEntry? = entries.firstOrNull { it.month == month }

/** Every entry except the start month's can be deleted; the start month holds the starting balance. */
fun Period.Monthly.canDeleteEntry(month: YearMonth): Boolean = month != startMonth && entryFor(month) != null

/** True once the account has an entry besides its start month's. */
val Period.Monthly.hasCheckIns: Boolean get() = entries.any { it.month != startMonth }

/** Up to [limit] entries, newest first, for My Finances' "Recent activity". */
fun Period.Monthly.recentEntries(limit: Int = RECENT_ENTRIES): List<MonthlyEntry> =
    entries.sortedByDescending { it.month }.take(limit)

const val RECENT_ENTRIES: Int = 5
