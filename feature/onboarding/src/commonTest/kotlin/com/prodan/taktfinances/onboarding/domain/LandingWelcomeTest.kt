package com.prodan.taktfinances.onboarding.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LandingWelcomeTest {

    @Test
    fun landingWelcome_returnsTitleAndMessage() {
        val welcome = landingWelcome()
        assertEquals("Welcome", welcome.title)
        assertEquals(
            "Takt Finances keeps a clear view of your money, so the next step is yours.",
            welcome.message,
        )
    }
}
