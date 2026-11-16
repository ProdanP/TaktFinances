package com.prodan.taktfinances.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.data.local.FinanceAccountEntity

/** The app's one database. Each feature owns its entities and DAOs and is listed here. */
@Database(entities = [FinanceAccountEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun financeAccountDao(): FinanceAccountDao
}

// Room generates the actual implementations for every target.
@Suppress("KotlinNoActualForExpect")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
