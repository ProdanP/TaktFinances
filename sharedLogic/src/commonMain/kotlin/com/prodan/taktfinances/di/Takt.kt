package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.accountsModule
import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.database.AppDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/** The app database and the DAOs features receive from it. */
internal fun databaseModule(database: AppDatabase): Module = module {
    single<AppDatabase> { database }
    single<FinanceAccountDao> { get<AppDatabase>().financeAccountDao() }
}

internal fun taktModules(database: AppDatabase): List<Module> = listOf(databaseModule(database), accountsModule)

/**
 * Builds the dependency graph once per process. A second call (activity recreation, previews)
 * keeps the running graph and does not open [database]. The shells call the platform entries
 * (`startTaktOnAndroid`, `startTaktOnDesktop`, `startTaktOnIos`), so they never see Room.
 * [extraModules] are modules from outside :sharedLogic, such as the ViewModels in :sharedUI.
 */
internal fun startTakt(extraModules: List<Module> = emptyList(), database: () -> AppDatabase) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin { modules(taktModules(database()) + extraModules) }
}
