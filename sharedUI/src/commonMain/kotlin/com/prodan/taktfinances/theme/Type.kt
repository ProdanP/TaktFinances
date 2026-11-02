package com.prodan.taktfinances.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import taktfinances.sharedui.generated.resources.Res
import taktfinances.sharedui.generated.resources.inter_medium
import taktfinances.sharedui.generated.resources.inter_regular

@Composable
fun AppTypography(): Typography {
    val inter = FontFamily(
        Font(Res.font.inter_regular, weight = FontWeight.Normal),
        Font(Res.font.inter_medium, weight = FontWeight.Medium),
    )
    val baseline = Typography()
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = inter),
        displayMedium = baseline.displayMedium.copy(fontFamily = inter),
        displaySmall = baseline.displaySmall.copy(fontFamily = inter),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = inter),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = inter),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = inter),
        titleLarge = baseline.titleLarge.copy(fontFamily = inter),
        titleMedium = baseline.titleMedium.copy(fontFamily = inter),
        titleSmall = baseline.titleSmall.copy(fontFamily = inter),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = inter),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = inter),
        bodySmall = baseline.bodySmall.copy(fontFamily = inter),
        labelLarge = baseline.labelLarge.copy(fontFamily = inter),
        labelMedium = baseline.labelMedium.copy(fontFamily = inter),
        labelSmall = baseline.labelSmall.copy(fontFamily = inter),
    )
}
