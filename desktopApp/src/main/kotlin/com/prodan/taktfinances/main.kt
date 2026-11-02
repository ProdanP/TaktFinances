package com.prodan.taktfinances

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.prodan.taktfinances.di.initKoin
import com.prodan.taktfinances.di.uiModule
import com.prodan.taktfinances.theme.ThemeContrast

fun main() {
    initKoin {
        modules(uiModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TaktFinances",
        ) {
            App(contrast = ThemeContrast.Standard)
        }
    }
}
