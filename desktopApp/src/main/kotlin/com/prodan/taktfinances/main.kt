package com.prodan.taktfinances

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.prodan.taktfinances.di.presentationModule
import com.prodan.taktfinances.di.startTaktOnDesktop
import com.prodan.taktfinances.theme.ThemeContrast

fun main() {
    startTaktOnDesktop(listOf(presentationModule))
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TaktFinances",
        ) {
            App(contrast = ThemeContrast.Standard)
        }
    }
}