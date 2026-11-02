package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MinorUnitsTest {
    @Test
    fun padsOneFractionDigit() {
        assertEquals(1250L, minorUnits("12.5"))
    }

    @Test
    fun twoFractionDigits() {
        assertEquals(1250L, minorUnits("12.50"))
    }

    @Test
    fun wholeUnitsBecomeZeroFraction() {
        assertEquals(1200L, minorUnits("12"))
    }

    @Test
    fun zeroIsValid() {
        assertEquals(0L, minorUnits("0"))
    }

    @Test
    fun rejectsThirdFractionDigit() {
        assertNull(minorUnits("12.505"))
    }

    @Test
    fun rejectsEmpty() {
        assertNull(minorUnits(""))
    }

    @Test
    fun rejectsNegative() {
        assertNull(minorUnits("-1"))
        assertNull(minorUnits("-12.50"))
    }
}
