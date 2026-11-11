package com.prodan.taktfinances.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancesColorSchemeTest {

    @Test
    fun selectorMatchesExport() {
        val light = financesColorScheme(dark = false, contrast = ThemeContrast.Standard)
        assertEquals(Color(0xFF1C6873), light.primary)
        assertEquals(Color(0xFFECFCFF), light.onPrimary)
        assertEquals(Color(0xFFA9EEFA), light.primaryContainer)
        assertEquals(Color(0xFFF6FAFB), light.background)
        assertEquals(Color(0xFF2A3436), light.onBackground)
        assertEquals(Color(0xFFF6FAFB), light.surface)
        assertEquals(Color(0xFFA83836), light.error)

        val dark = financesColorScheme(dark = true, contrast = ThemeContrast.Standard)
        assertEquals(Color(0xFF9DCFD8), dark.primary)
        assertEquals(Color(0xFF0E474F), dark.onPrimary)
        assertEquals(Color(0xFF265962), dark.primaryContainer)
        assertEquals(Color(0xFF0A0F10), dark.background)
        assertEquals(Color(0xFFDDE7E9), dark.onBackground)
        assertEquals(Color(0xFF0A0F10), dark.surface)
        assertEquals(Color(0xFFFA746F), dark.error)

        assertEquals(
            Color(0xFF00282E),
            financesColorScheme(dark = false, contrast = ThemeContrast.High).primary,
        )
        assertEquals(
            Color(0xFF4B7C85),
            financesColorScheme(dark = true, contrast = ThemeContrast.Medium).primaryContainer,
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
