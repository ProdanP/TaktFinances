package com.prodan.taktfinances.home

sealed interface HomeEffect {
    data object OpenAddAccount : HomeEffect

    /** Opens Account Detail for [id]; [checkIn] also opens its check-in sheet for the current month. */
    data class OpenAccount(val id: Long, val checkIn: Boolean) : HomeEffect
}
