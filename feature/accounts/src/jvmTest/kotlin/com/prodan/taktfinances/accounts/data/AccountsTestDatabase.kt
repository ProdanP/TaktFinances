package com.prodan.taktfinances.accounts.data

import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.data.local.FinanceAccountEntity

/**
 * Holds only this feature's tables, so repository tests don't need the app database. JVM-only,
 * so Room finds the generated implementation by reflection and no `@ConstructedBy` is needed.
 */
@Database(entities = [FinanceAccountEntity::class], version = 1, exportSchema = false)
abstract class AccountsTestDatabase : RoomDatabase() {
    abstract fun financeAccountDao(): FinanceAccountDao
}

fun inMemoryAccountsDatabase(): AccountsTestDatabase =
    Room.inMemoryDatabaseBuilder<AccountsTestDatabase>().setDriver(BundledSQLiteDriver()).build()
