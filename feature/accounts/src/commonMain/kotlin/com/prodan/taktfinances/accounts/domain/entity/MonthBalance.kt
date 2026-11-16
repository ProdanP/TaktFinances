package com.prodan.taktfinances.accounts.domain.entity

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth

/** An account's balance at the end of [month]. [checkedIn] is false when a Monthly account has no entry for it. */
data class MonthBalance(val month: YearMonth, val balanceMinor: Long, val checkedIn: Boolean)

/** Change against the previous month. [percentTenths] is tenths of a percent (150 = 15.0%), null when the previous balance is 0. */
data class MonthChange(val amountMinor: Long, val percentTenths: Long?)

/**
 * Month-end balances from the account's first month through [today]'s month, oldest first. A Monthly
 * month without an entry carries the latest earlier entry forward; it is never 0 for lack of an entry.
 */
fun monthEndBalances(account: SavedAccount, today: LocalDate): List<MonthBalance> {
    val period = account.period
    val months = monthsBetween(period.startMonth, today.yearMonth)
    return when (period) {
        is Period.Monthly -> {
            val byMonth = period.entries.associateBy { it.month }
            var carried = 0L
            months.map { month ->
                val entry = byMonth[month]
                if (entry != null) carried = entry.amountMinor
                MonthBalance(month, carried, checkedIn = entry != null)
            }
        }
        is Period.Transactions -> months.map { MonthBalance(it, period.openingBalanceMinor, checkedIn = true) }
    }
}

/** The account's balance now: its month-end balance for [today]'s month. */
fun SavedAccount.balanceMinor(today: LocalDate): Long = monthEndBalances(this, today).last().balanceMinor

/** [current] minus [previous]; null for an account's first month (no [previous]). */
fun monthChange(previous: Long?, current: Long): MonthChange? {
    previous ?: return null
    val amount = current - previous
    val percent = if (previous == 0L) null else (amount * 1000.0 / abs(previous)).roundToLong()
    return MonthChange(amount, percent)
}

/** Changes for each of [balances] (oldest first), in the same order; the first is null. */
fun monthChanges(balances: List<MonthBalance>): List<MonthChange?> =
    balances.mapIndexed { index, balance -> monthChange(balances.getOrNull(index - 1)?.balanceMinor, balance.balanceMinor) }

/** Months a check-in may be entered for: [today]'s month back to the start month, newest first. */
fun checkInMonths(period: Period.Monthly, today: LocalDate): List<YearMonth> =
    monthsBetween(period.startMonth, today.yearMonth).reversed()

/** [first] through [last], oldest first; just [first] when the clock reads a date before it. */
private fun monthsBetween(first: YearMonth, last: YearMonth): List<YearMonth> {
    if (last <= first) return listOf(first)
    return generateSequence(first) { it.plusMonth() }.takeWhile { it <= last }.toList()
}

/** The month before [month]. */
fun previousMonth(month: YearMonth): YearMonth = month.minusMonth()
