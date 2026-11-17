package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount

/** What Account Detail shows below its top bar. */
internal sealed interface AccountDetailContent {
    data object Loading : AccountDetailContent

    /** The account could not be read; the user can retry. */
    data object Failed : AccountDetailContent

    /** [timeline] is null for an account tracked by trades ("No trades yet", no check-in). */
    data class Loaded(
        val account: SavedAccount,
        val header: AccountDetailHeaderModel,
        val balances: List<MonthBalance>,
        val timeline: List<TimelineRowModel>?,
    ) : AccountDetailContent {
        val canCheckIn: Boolean get() = timeline != null
    }
}
