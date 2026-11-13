package com.prodan.taktfinances

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.prodan.taktfinances.di.AppDependencies
import com.prodan.taktfinances.di.desktopDataDirectory
import com.prodan.taktfinances.di.jvmDatabaseDriver
import com.prodan.taktfinances.di.startTakt
import com.prodan.taktfinances.theme.ThemeContrast

fun main() {
    startTakt(jvmDatabaseDriver(desktopDataDirectory()))
    val dependencies = AppDependencies()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TaktFinances",
        ) {
            App(dependencies = dependencies, contrast = ThemeContrast.Standard)
        }
    }
}