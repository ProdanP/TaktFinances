package com.prodan.taktfinances.core

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutcomeOfTest {
    @Test
    fun valueIsOk() {
        assertEquals(Outcome.Ok("saved"), outcomeOf { "saved" })
    }

    @Test
    fun exceptionIsUnavailable() {
        assertEquals(Outcome.Failed(Reason.Unavailable), outcomeOf<String> { throw IllegalStateException("closed") })
    }

    @Test
    fun cancellationIsRethrown() {
        assertFailsWith<CancellationException> { outcomeOf<String> { throw CancellationException("cancelled") } }
    }
}
