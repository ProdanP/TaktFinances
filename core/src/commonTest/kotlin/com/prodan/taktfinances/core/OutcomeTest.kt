package com.prodan.taktfinances.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OutcomeTest {
    @Test
    fun okCarriesValue() {
        val outcome: Outcome<String> = Outcome.Ok("saved")
        assertEquals("saved", assertIs<Outcome.Ok<String>>(outcome).value)
    }

    @Test
    fun failedCarriesReason() {
        val outcome: Outcome<String> = Outcome.Failed(Reason.Unavailable)
        assertEquals(Reason.Unavailable, assertIs<Outcome.Failed>(outcome).reason)
    }
}
