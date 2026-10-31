package com.prodan.taktfinances

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.themeContrast

fun Context.readThemeContrast(): ThemeContrast {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        return ThemeContrast.Standard
    }
    val manager = getSystemService(UiModeManager::class.java) ?: return ThemeContrast.Standard
    return themeContrast(manager.contrast)
}
