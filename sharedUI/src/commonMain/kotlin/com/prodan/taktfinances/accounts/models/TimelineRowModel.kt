package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.monthChanges
import com.prodan.taktfinances.accounts.domain.entity.monthLabel
import kotlinx.datetime.YearMonth

internal data class TimelineRowModel(
    val month: YearMonth,
    val label: String,
    val balance: String,
    /** Null on the start month. */
    val change: ChangeModel?,
    val checkedIn: Boolean,
)

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
