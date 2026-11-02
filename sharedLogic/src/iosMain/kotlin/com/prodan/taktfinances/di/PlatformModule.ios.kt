package com.prodan.taktfinances.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.prodan.taktfinances.accounts.data.FinancesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        NativeSqliteDriver(
            schema = FinancesDatabase.Schema,
            name = "finances.db",
        )
    }
}
