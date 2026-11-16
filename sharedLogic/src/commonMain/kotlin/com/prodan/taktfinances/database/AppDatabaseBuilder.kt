package com.prodan.taktfinances.database

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** File name of the app's one SQLite database on every platform. */
internal const val DATABASE_NAME: String = "taktfinances.db"

/**
 * The database file of pre-Room builds. Its data is not carried over; the platform
 * builders delete it (and its journal files) before opening [DATABASE_NAME].
 */
internal const val LEGACY_DATABASE_NAME: String = "takt.db"

/** [LEGACY_DATABASE_NAME] and the SQLite journal files that can sit next to it. */
internal val legacyDatabaseFiles: List<String> =
    listOf("", "-journal", "-wal", "-shm").map { LEGACY_DATABASE_NAME + it }

/** The same SQLite on every platform; DAO calls run on [Dispatchers.IO]. */
internal fun RoomDatabase.Builder<AppDatabase>.buildApp(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
