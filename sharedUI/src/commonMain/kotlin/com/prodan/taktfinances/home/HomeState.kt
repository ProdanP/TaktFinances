package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.models.MyFinancesContent

data class HomeState(
    val finances: MyFinancesContent = MyFinancesContent.Loading,
)
