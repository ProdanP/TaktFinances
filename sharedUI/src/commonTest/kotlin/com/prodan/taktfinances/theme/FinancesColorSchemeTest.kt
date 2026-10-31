package com.prodan.taktfinances.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancesColorSchemeTest {

    @Test
    fun selectorMatchesExport() {
        val light = financesColorScheme(dark = false, contrast = ThemeContrast.Standard)
        assertEquals(Color(0xFF415F91), light.primary)
        assertEquals(Color(0xFFFFFFFF), light.onPrimary)
        assertEquals(Color(0xFFD6E3FF), light.primaryContainer)
        assertEquals(Color(0xFFF9F9FF), light.background)
        assertEquals(Color(0xFF191C20), light.onBackground)
        assertEquals(Color(0xFFF9F9FF), light.surface)
        assertEquals(Color(0xFFBA1A1A), light.error)

        val dark = financesColorScheme(dark = true, contrast = ThemeContrast.Standard)
        assertEquals(Color(0xFFAAC7FF), dark.primary)
        assertEquals(Color(0xFF0A305F), dark.onPrimary)
        assertEquals(Color(0xFF284777), dark.primaryContainer)
        assertEquals(Color(0xFF111318), dark.background)
        assertEquals(Color(0xFFE2E2E9), dark.onBackground)
        assertEquals(Color(0xFF111318), dark.surface)
        assertEquals(Color(0xFFFFB4AB), dark.error)

        assertEquals(
            Color(0xFF032B5B),
            financesColorScheme(dark = false, contrast = ThemeContrast.High).primary,
        )
        assertEquals(
            Color(0xFFCDDDFF),
            financesColorScheme(dark = true, contrast = ThemeContrast.Medium).primary,
        )
    }

    @Test
    fun contrastThresholds() {
        assertEquals(ThemeContrast.Standard, themeContrast(0f))
        assertEquals(ThemeContrast.Standard, themeContrast(0.33f))
        assertEquals(ThemeContrast.Medium, themeContrast(0.34f))
        assertEquals(ThemeContrast.Medium, themeContrast(0.66f))
        assertEquals(ThemeContrast.High, themeContrast(0.67f))
        assertEquals(ThemeContrast.High, themeContrast(1f))
    }
}
