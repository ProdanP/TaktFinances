package com.prodan.taktfinances.di

import androidx.room3.Room
import com.prodan.taktfinances.database.AppDatabase
import com.prodan.taktfinances.database.DATABASE_NAME
import com.prodan.taktfinances.database.buildApp
import com.prodan.taktfinances.database.legacyDatabaseFiles
import java.io.File
import org.koin.core.module.Module

/** Entry for the desktop shell: opens the app database and starts the dependency graph with [extraModules]. */
fun startTaktOnDesktop(extraModules: List<Module> = emptyList()) =
    startTakt(extraModules) { jvmAppDatabase(desktopDataDirectory()) }

/** Opens (and on first use creates) the database in [directory], after removing the legacy file. */
internal fun jvmAppDatabase(directory: File): AppDatabase {
    directory.mkdirs()
    legacyDatabaseFiles.forEach { File(directory, it).delete() }
    return Room.databaseBuilder<AppDatabase>(File(directory, DATABASE_NAME).absolutePath).buildApp()
}

/** The per-user app data directory for the desktop app. */
internal fun desktopDataDirectory(): File {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        "mac" in os -> File(home, "Library/Application Support/TaktFinances")
        "win" in os -> File(System.getenv("APPDATA") ?: home, "TaktFinances")
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "TaktFinances")
    }
}
