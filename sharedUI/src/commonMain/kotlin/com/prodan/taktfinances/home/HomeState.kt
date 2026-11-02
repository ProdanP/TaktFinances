package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.domain.HomeDestination

data class HomeState(
    val destinations: List<HomeDestination> = emptyList(),
    val selected: HomeDestination = HomeDestination.Dashboard,
)
