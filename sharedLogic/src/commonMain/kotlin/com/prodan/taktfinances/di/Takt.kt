package com.prodan.taktfinances.di

import app.cash.sqldelight.db.SqlDriver
import com.prodan.taktfinances.accounts.createAccountRepository
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.db.AppDatabase
import org.koin.mp.KoinPlatform
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/** File name of the app's one SQLite database on every platform. */
const val DATABASE_NAME: String = "takt.db"

/**
 * The app database's driver. Platform drivers are created with [AppDatabase.Schema], so they
 * create the tables on first open.
 */
internal fun databaseModule(driver: SqlDriver): Module = module {
    single<SqlDriver> { driver }
}

internal val accountsModule: Module = module {
    single<AccountRepository> { createAccountRepository(get()) }
}

internal fun taktModules(driver: SqlDriver): List<Module> = listOf(databaseModule(driver), accountsModule)

/**
 * Builds the dependency graph once per process. A second call (activity recreation, previews)
 * keeps the running graph and ignores [driver].
 */
fun startTakt(driver: SqlDriver) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin { modules(taktModules(driver)) }
}

/** Schema of the app database, for the platform drivers. */
internal val appSchema = AppDatabase.Schema
