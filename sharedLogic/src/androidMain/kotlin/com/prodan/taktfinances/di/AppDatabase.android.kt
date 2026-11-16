package com.prodan.taktfinances.di

import android.content.Context
import androidx.room3.Room
import com.prodan.taktfinances.database.AppDatabase
import com.prodan.taktfinances.database.DATABASE_NAME
import com.prodan.taktfinances.database.LEGACY_DATABASE_NAME
import com.prodan.taktfinances.database.buildApp
import org.koin.core.module.Module

/** Entry for the Android shell: opens the app database and starts the dependency graph with [extraModules]. */
fun startTaktOnAndroid(context: Context, extraModules: List<Module> = emptyList()) =
    startTakt(extraModules) { androidAppDatabase(context.applicationContext) }

/** Opens the database in the app's database directory, after removing the legacy file. */
internal fun androidAppDatabase(context: Context): AppDatabase {
    context.deleteDatabase(LEGACY_DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(context, context.getDatabasePath(DATABASE_NAME).absolutePath).buildApp()
}
