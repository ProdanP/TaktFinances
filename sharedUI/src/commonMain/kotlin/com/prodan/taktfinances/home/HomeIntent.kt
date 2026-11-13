package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.domain.HomeDestination

sealed interface HomeIntent {
    data class DestinationSelected(val destination: HomeDestination) : HomeIntent
    data object AddAccountTapped : HomeIntent

    /** The Home screen became visible (first start or returning from another screen). */
    data object Shown : HomeIntent

    /** The swiper settled on, or the user picked, the account with [id]. */
    data class AccountSelected(val id: Long) : HomeIntent
    data object PreviousTapped : HomeIntent
    data object NextTapped : HomeIntent
    data object RetryTapped : HomeIntent
    data object AddFirstAccountTapped : HomeIntent
}
