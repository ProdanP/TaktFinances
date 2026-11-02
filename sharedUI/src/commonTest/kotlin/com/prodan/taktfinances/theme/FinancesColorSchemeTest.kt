package com.prodan.taktfinances.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancesColorSchemeTest {

    @Test
    fun selectorMatchesExport() {
        val light = financesColorScheme(dark = false)
        assertEquals(Color(0xFF39608F), light.primary)
        assertEquals(Color(0xFFFFFFFF), light.onPrimary)
        assertEquals(Color(0xFFD3E4FF), light.primaryContainer)
        assertEquals(Color(0xFFF8F9FF), light.background)
        assertEquals(Color(0xFF191C20), light.onBackground)
        assertEquals(Color(0xFFF8F9FF), light.surface)
        assertEquals(Color(0xFFBA1A1A), light.error)
        assertEquals(Color(0xFFD3E4FF), light.primaryFixed)
        assertEquals(Color(0xFF001C38), light.onPrimaryFixed)

        val dark = financesColorScheme(dark = true)
        assertEquals(Color(0xFFA3C9FE), dark.primary)
        assertEquals(Color(0xFF00315B), dark.onPrimary)
        assertEquals(Color(0xFF1E4876), dark.primaryContainer)
        assertEquals(Color(0xFF111418), dark.background)
        assertEquals(Color(0xFFE1E2E8), dark.onBackground)
        assertEquals(Color(0xFF111418), dark.surface)
        assertEquals(Color(0xFFFFB4AB), dark.error)
        assertEquals(Color(0xFFD3E4FF), dark.primaryFixed)
        assertEquals(Color(0xFF001C38), dark.onPrimaryFixed)
    }
}
