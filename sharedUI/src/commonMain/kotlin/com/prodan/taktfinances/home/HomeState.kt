package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.domain.HomeDestination
import com.prodan.taktfinances.home.models.MyFinancesContent

data class HomeState(
    val destinations: List<HomeDestination> = emptyList(),
    val selected: HomeDestination = HomeDestination.Dashboard,
    val finances: MyFinancesContent = MyFinancesContent.Loading,
)
