package com.prodan.taktfinances.home

sealed interface HomeEffect {
    data object OpenAddAccount : HomeEffect
}
