package com.prodan.taktfinances.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

fun nativeDatabaseDriver(): SqlDriver = NativeSqliteDriver(appSchema, DATABASE_NAME)
