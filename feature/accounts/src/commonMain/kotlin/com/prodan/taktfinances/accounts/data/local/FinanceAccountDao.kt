package com.prodan.taktfinances.accounts.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/** Public only because the app database in :sharedLogic exposes it; it never leaves `data`. */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Dao
interface FinanceAccountDao {
    /** Inserts [row] (its id is ignored) and returns the new row id. */
    @Insert
    suspend fun insert(row: FinanceAccountEntity): Long

    @Query("SELECT * FROM finance_account ORDER BY id")
    suspend fun selectAll(): List<FinanceAccountEntity>
}
