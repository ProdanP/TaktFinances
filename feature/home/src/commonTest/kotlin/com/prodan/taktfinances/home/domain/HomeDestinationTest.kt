package com.prodan.taktfinances.home.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeDestinationTest {

    @Test
    fun homeDestinations_returnsTitlesInOrder() {
        val destinations = homeDestinations()
        assertEquals(
            listOf("Dashboard", "My Finances", "Profile"),
            destinations.map { it.title },
        )
    }
}
