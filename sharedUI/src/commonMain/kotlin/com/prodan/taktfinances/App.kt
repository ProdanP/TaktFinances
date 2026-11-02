package com.prodan.taktfinances

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.prodan.taktfinances.onboarding.LandingScreen
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme

@Composable
@Preview
fun App(contrast: ThemeContrast = ThemeContrast.Standard) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(colorScheme = financesColorScheme(dark = dark, contrast = contrast)) {
        LandingScreen()
    }
}
