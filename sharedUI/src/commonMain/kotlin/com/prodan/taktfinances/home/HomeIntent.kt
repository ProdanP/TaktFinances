package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.domain.HomeDestination

sealed interface HomeIntent {
    data class DestinationSelected(val destination: HomeDestination) : HomeIntent
    data object AddAccountTapped : HomeIntent
}
