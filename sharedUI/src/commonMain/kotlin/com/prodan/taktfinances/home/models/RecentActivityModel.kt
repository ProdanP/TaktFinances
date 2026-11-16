package com.prodan.taktfinances.home.models

import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.hasCheckIns
import com.prodan.taktfinances.accounts.domain.entity.monthLabel
import com.prodan.taktfinances.accounts.domain.entity.recentEntries

/** What "Recent activity" shows for the selected account. */
sealed interface RecentActivityModel {
    /** Newest first, at most five; followed by "See all". */
    data class Entries(val rows: List<ActivityRow>, val currency: String) : RecentActivityModel

    /** A Monthly account with only its start month: "No check-ins yet" and a "Check in" action. */
    data object NoCheckIns : RecentActivityModel

    /** An account tracked by trades that has none yet: "No trades yet", no action. */
    data object NoTrades : RecentActivityModel
}

data class ActivityRow(val month: String, val amount: String)

internal fun SavedAccount.toRecentActivity(): RecentActivityModel = when (val period = period) {
    is Period.Transactions -> RecentActivityModel.NoTrades
    is Period.Monthly -> if (!period.hasCheckIns) {
        RecentActivityModel.NoCheckIns
    } else {
        RecentActivityModel.Entries(
            rows = period.recentEntries().map { ActivityRow(monthLabel(it.month), formatAmount(it.amountMinor)) },
            currency = account.currency.code,
        )
    }
}
