package com.prodan.taktfinances.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

/** Opens (and on first use creates) the database in [directory]. */
fun jvmDatabaseDriver(directory: File): SqlDriver {
    directory.mkdirs()
    return JdbcSqliteDriver("jdbc:sqlite:${File(directory, DATABASE_NAME).absolutePath}", Properties(), appSchema)
}

/** The per-user app data directory for the desktop app. */
fun desktopDataDirectory(): File {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        "mac" in os -> File(home, "Library/Application Support/TaktFinances")
        "win" in os -> File(System.getenv("APPDATA") ?: home, "TaktFinances")
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "TaktFinances")
    }
}
