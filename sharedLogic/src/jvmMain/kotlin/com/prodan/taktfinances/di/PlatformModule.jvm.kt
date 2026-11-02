package com.prodan.taktfinances.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.prodan.taktfinances.accounts.data.FinancesDatabase
import java.io.File
import java.util.Properties
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        val dbFile = File(System.getProperty("user.home"), ".taktfinances/finances.db")
        dbFile.parentFile?.mkdirs()
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties(),
            schema = FinancesDatabase.Schema,
        )
    }
}
