package com.prodan.taktfinances.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.prodan.taktfinances.accounts.data.FinancesDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        AndroidSqliteDriver(
            schema = FinancesDatabase.Schema,
            context = androidContext(),
            name = "finances.db",
        )
    }
}
