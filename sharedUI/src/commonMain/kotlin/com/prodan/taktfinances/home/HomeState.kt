package com.prodan.taktfinances.home

import com.prodan.taktfinances.accounts.domain.SavedAccount
import com.prodan.taktfinances.home.domain.HomeDestination

data class HomeState(
    val destinations: List<HomeDestination> = emptyList(),
    val selected: HomeDestination = HomeDestination.Dashboard,
    val finances: MyFinancesContent = MyFinancesContent.Loading,
)

/** What the My Finances tab shows. */
sealed interface MyFinancesContent {
    /** The first read has not finished yet. */
    data object Loading : MyFinancesContent

    /** The accounts could not be read; the user can retry. */
    data object Failed : MyFinancesContent

    /** No account exists yet. */
    data object Empty : MyFinancesContent

    /** [accounts] oldest first; [selectedId] is one of their ids. */
    data class Accounts(
        val accounts: List<SavedAccount>,
        val selectedId: Long,
    ) : MyFinancesContent {
        val selectedIndex: Int get() = accounts.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
        val canSelectPrevious: Boolean get() = selectedIndex > 0
        val canSelectNext: Boolean get() = selectedIndex < accounts.lastIndex
    }
}
