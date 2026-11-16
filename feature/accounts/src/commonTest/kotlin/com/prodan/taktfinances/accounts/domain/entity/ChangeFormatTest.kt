package com.prodan.taktfinances.accounts.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.YearMonth

class ChangeFormatTest {
    @Test
    fun signedAmounts() {
        assertEquals("+1,500.00", formatSignedAmount(150_000))
        assertEquals("−500.00", formatSignedAmount(-50_000))
        assertEquals("0.00", formatSignedAmount(0))
    }

    @Test
    fun percents() {
        assertEquals("+15.0%", formatPercent(150))
        assertEquals("−5.0%", formatPercent(-50))
        assertEquals("0.0%", formatPercent(0))
        assertEquals("+14.2%", formatPercent(monthChange(1_081_000, 1_234_000)!!.percentTenths!!))
    }

    @Test
    fun changes() {
        assertEquals("+1,530.00 (+14.2%)", formatChange(monthChange(1_081_000, 1_234_000)!!))
        assertEquals("+500.00", formatChange(MonthChange(50_000, null)))
    }

    @Test
    fun monthLabels() {
        assertEquals("November 2026", monthLabel(YearMonth(2026, 11)))
        assertEquals("October", shortMonthLabel(YearMonth(2026, 10)))
        assertEquals("Jan", monthAbbreviation(YearMonth(2027, 1)))
    }

    @Test
    fun amountCheck() {
        assertTrue(isAmount("11500,5"))
        assertTrue(isAmount("12."))
        assertFalse(isAmount(""))
        assertFalse(isAmount("abc"))
    }
}
