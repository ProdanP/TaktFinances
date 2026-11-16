package com.prodan.taktfinances.home.models

import com.prodan.taktfinances.accounts.domain.entity.SavedAccount

/** What the My Finances tab shows. */
sealed interface MyFinancesContent {
    /** The first read has not finished yet. */
    data object Loading : MyFinancesContent

    /** The accounts could not be read; the user can retry. */
    data object Failed : MyFinancesContent

    /** No account exists yet. */
    data object Empty : MyFinancesContent

    /** [accounts] oldest first; [selectedId] is one of their ids; [balances] is each account's current balance by id. */
    data class Accounts(
        val accounts: List<SavedAccount>,
        val selectedId: Long,
        val balances: Map<Long, Long> = emptyMap(),
    ) : MyFinancesContent {
        val selectedIndex: Int get() = accounts.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
        val canSelectPrevious: Boolean get() = selectedIndex > 0
        val canSelectNext: Boolean get() = selectedIndex < accounts.lastIndex
        val selected: SavedAccount get() = accounts[selectedIndex]
        val activity: RecentActivityModel get() = selected.toRecentActivity()
    }
}
