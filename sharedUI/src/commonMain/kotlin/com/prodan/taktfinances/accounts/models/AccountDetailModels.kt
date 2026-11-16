package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.MonthChange
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.accountSummary
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.formatChange
import com.prodan.taktfinances.accounts.domain.entity.monthAbbreviation
import com.prodan.taktfinances.accounts.domain.entity.monthChanges
import com.prodan.taktfinances.accounts.domain.entity.monthLabel
import com.prodan.taktfinances.theme.ChangeDirection
import kotlin.math.abs
import kotlinx.datetime.YearMonth

/** A signed change ready to show: "+1,530.00 (+14.2%)", its direction and a spoken label. */
data class ChangeModel(val text: String, val direction: ChangeDirection, val spoken: String)

data class AccountDetailHeaderModel(
    val name: String,
    val type: AccountType,
    val bank: Bank?,
    val summary: String,
    val last4: String?,
    val balance: String,
    val currency: String,
    /** Null when the account has no previous month ("This month —"). */
    val thisMonth: ChangeModel?,
)

data class TimelineRowModel(
    val month: YearMonth,
    val label: String,
    val balance: String,
    /** Null on the start month. */
    val change: ChangeModel?,
    val checkedIn: Boolean,
)

data class ChartPointModel(val month: YearMonth, val axisLabel: String, val label: String, val balanceMinor: Long, val balance: String)

/** How many months the chart shows; [months] null means all. */
enum class ChartRange(val title: String, val months: Int?) {
    SixMonths("6M", 6),
    OneYear("1Y", 12),
    All("All", null),
}

internal fun MonthChange.toUi(currency: String): ChangeModel {
    val direction = when {
        amountMinor > 0 -> ChangeDirection.Up
        amountMinor < 0 -> ChangeDirection.Down
        else -> ChangeDirection.Flat
    }
    val word = when (direction) {
        ChangeDirection.Up -> "Increase of"
        ChangeDirection.Down -> "Decrease of"
        ChangeDirection.Flat -> "No change,"
    }
    val percent = percentTenths?.let { ", ${abs(it) / 10}.${abs(it) % 10} percent" }.orEmpty()
    return ChangeModel(formatChange(this), direction, "$word ${formatAmount(abs(amountMinor))} $currency$percent")
}

internal fun SavedAccount.toHeader(balances: List<MonthBalance>): AccountDetailHeaderModel {
    val currency = account.currency.code
    val bank = account.details as? AccountDetails.BankDetails
    return AccountDetailHeaderModel(
        name = account.name,
        type = account.type,
        bank = bank?.bank,
        summary = accountSummary(account.details),
        last4 = bank?.last4,
        balance = formatAmount(balances.last().balanceMinor),
        currency = currency,
        thisMonth = monthChanges(balances).last()?.toUi(currency),
    )
}

/** Monthly accounts only: one row per month, newest first. Null for accounts tracked by trades. */
internal fun SavedAccount.toTimeline(balances: List<MonthBalance>): List<TimelineRowModel>? {
    if (period !is Period.Monthly) return null
    val currency = account.currency.code
    val changes = monthChanges(balances)
    return balances.indices.reversed().map { i ->
        val balance = balances[i]
        TimelineRowModel(
            month = balance.month,
            label = monthLabel(balance.month),
            balance = formatAmount(balance.balanceMinor),
            change = changes[i]?.toUi(currency),
            checkedIn = balance.checkedIn,
        )
    }
}

internal fun List<MonthBalance>.toChartPoints(range: ChartRange): List<ChartPointModel> =
    (range.months?.let { takeLast(it) } ?: this).map {
        ChartPointModel(it.month, monthAbbreviation(it.month), monthLabel(it.month), it.balanceMinor, formatAmount(it.balanceMinor))
    }

/** Spoken summary of the chart, e.g. "Balance, 6 months: June 2026 8,200.00 MDL to November 2026 12,340.00 MDL". */
internal fun List<ChartPointModel>.summary(currency: String): String {
    val first = first()
    val last = last()
    if (size == 1) return "Balance for ${first.label}: ${first.balance} $currency"
    return "Balance, $size months: ${first.label} ${first.balance} $currency to ${last.label} ${last.balance} $currency"
}
