package com.prodan.taktfinances.theme

enum class ThemeContrast {
    Standard,
    Medium,
    High,
}

fun themeContrast(level: Float): ThemeContrast = when {
    level < 0.34f -> ThemeContrast.Standard
    level < 0.67f -> ThemeContrast.Medium
    else -> ThemeContrast.High
}
