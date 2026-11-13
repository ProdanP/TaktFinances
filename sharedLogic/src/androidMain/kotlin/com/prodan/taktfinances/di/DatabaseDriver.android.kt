package com.prodan.taktfinances.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun androidDatabaseDriver(context: Context): SqlDriver =
    AndroidSqliteDriver(appSchema, context.applicationContext, DATABASE_NAME)
