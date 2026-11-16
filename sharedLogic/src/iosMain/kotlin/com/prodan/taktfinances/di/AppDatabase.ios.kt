package com.prodan.taktfinances.di

import androidx.room3.Room
import com.prodan.taktfinances.database.AppDatabase
import com.prodan.taktfinances.database.DATABASE_NAME
import com.prodan.taktfinances.database.buildApp
import com.prodan.taktfinances.database.legacyDatabaseFiles
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/** Opens the database in Application Support/databases (where earlier builds kept theirs), after removing the legacy file. */
@OptIn(ExperimentalForeignApi::class)
internal fun iosAppDatabase(): AppDatabase {
    val directory = databaseDirectory()
    val files = NSFileManager.defaultManager
    legacyDatabaseFiles.forEach { name ->
        val path = "$directory/$name"
        if (files.fileExistsAtPath(path)) files.removeItemAtPath(path, null)
    }
    return Room.databaseBuilder<AppDatabase>("$directory/$DATABASE_NAME").buildApp()
}

@OptIn(ExperimentalForeignApi::class)
private fun databaseDirectory(): String {
    val support: NSURL = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(NSApplicationSupportDirectory, NSUserDomainMask, null, true, null),
    ) { "Application Support directory is unavailable" }
    val directory = requireNotNull(support.URLByAppendingPathComponent("databases")?.path)
    NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)
    return directory
}
