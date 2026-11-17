package com.prodan.taktfinances.home

internal sealed interface HomeIntent {
    data object AddAccountTapped : HomeIntent

    /** The Home screen became visible (first start or returning from another screen). */
    data object Shown : HomeIntent

    /** The swiper settled on, or the user picked, the account with [id]. */
    data class AccountSelected(val id: Long) : HomeIntent
    data object PreviousTapped : HomeIntent
    data object NextTapped : HomeIntent
    data object RetryTapped : HomeIntent
    data object AddFirstAccountTapped : HomeIntent

    /** The card of the account with [id] was tapped (or Enter on the focused card). */
    data class AccountTapped(val id: Long) : HomeIntent

    /** "See all" under the selected account's activity. */
    data class SeeAllTapped(val id: Long) : HomeIntent

    /** "Check in" in the selected account's empty activity. */
    data class CheckInTapped(val id: Long) : HomeIntent
}
