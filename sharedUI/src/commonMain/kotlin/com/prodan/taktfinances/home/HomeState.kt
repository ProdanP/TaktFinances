package com.prodan.taktfinances.home

import com.prodan.taktfinances.home.models.MyFinancesContent

internal data class HomeState(
    val finances: MyFinancesContent = MyFinancesContent.Loading,
)
