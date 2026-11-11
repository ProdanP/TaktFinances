package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AmountInputTest {
    @Test
    fun commaBecomesTheSeparator() {
        assertEquals("2500.5", filterAmountInput("2500,5"))
    }

    @Test
    fun thirdDecimalIsDropped() {
        assertEquals("3.99", filterAmountInput("3.999"))
    }

    @Test
    fun lettersAndSignsAreDropped() {
        assertEquals("12.5", filterAmountInput("-12a.5"))
    }

    @Test
    fun secondSeparatorIsDropped() {
        assertEquals("1.23", filterAmountInput("1.2.3"))
    }

    @Test
    fun leadingSeparatorGetsAZero() {
        assertEquals("0.5", filterAmountInput(".5"))
        assertEquals("0.", filterAmountInput(","))
    }

    @Test
    fun parsesToMinorUnits() {
        assertEquals(250050, parseAmountMinor("2500.5"))
        assertEquals(250050, parseAmountMinor("2500,50"))
        assertEquals(1200, parseAmountMinor("12."))
        assertEquals(0, parseAmountMinor("0"))
    }

    @Test
    fun rejectsInvalidAmounts() {
        for (value in listOf("", "  ", "abc", "-100", "1.234", ".5", "1.2.3", "99999999999999999999")) {
            assertNull(parseAmountMinor(value), value)
        }
    }

    @Test
    fun formatsWithThousandsAndTwoDecimals() {
        assertEquals("2,500.00", formatAmount(250000))
        assertEquals("2,500.50", formatAmount(250050))
        assertEquals("1,234,567.80", formatAmount(123456780))
        assertEquals("0.00", formatAmount(0))
        assertEquals("999.05", formatAmount(99905))
    }

    @Test
    fun formatsRawInput() {
        assertEquals("2,500.00", formatAmountInput("2500"))
        assertEquals("2,500.50", formatAmountInput("2500.5"))
        assertEquals("1,234,567.80", formatAmountInput("1234567.8"))
        assertEquals("0.00", formatAmountInput("0"))
        assertEquals("12.00", formatAmountInput("12."))
        assertNull(formatAmountInput(""))
    }
}
