package com.prodan.taktfinances.accounts.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Accounts, their tracking periods and monthly entries: one aggregate, so one DAO.
 * Public only because the app database in :sharedLogic exposes it; it never leaves `data`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Dao
interface FinanceAccountDao {
    /** Inserts [row] (its id is ignored) and returns the new row id. */
    @Insert
    suspend fun insert(row: FinanceAccountEntity): Long

    /** Inserts [row] (its id is ignored) and returns the new row id. */
    @Insert
    suspend fun insertPeriod(row: AccountPeriodEntity): Long

    /** Inserts [row], or replaces the row for the same period and month. */
    @Upsert
    suspend fun upsertEntry(row: MonthlyEntryEntity)

    /**
     * Inserts [account], its first [period] and, for a Monthly period, its first [entry], all or nothing.
     * The ids in [period] and [entry] are ignored. Returns the new account id and period id.
     */
    @Transaction
    suspend fun insertWithStart(
        account: FinanceAccountEntity,
        period: AccountPeriodEntity,
        entry: MonthlyEntryEntity?,
    ): Pair<Long, Long> {
        val accountId = insert(account)
        val periodId = insertPeriod(
            AccountPeriodEntity(0, accountId, period.mode, period.startDate, period.endDate, period.openingBalanceMinor),
        )
        if (entry != null) upsertEntry(MonthlyEntryEntity(periodId, entry.month, entry.amountMinor))
        return accountId to periodId
    }

    @Query("DELETE FROM monthly_entry WHERE period_id = :periodId AND month = :month")
    suspend fun deleteEntry(periodId: Long, month: String)

    @Query("SELECT * FROM finance_account ORDER BY id")
    suspend fun selectAll(): List<FinanceAccountEntity>

    @Query("SELECT * FROM finance_account WHERE id = :id")
    suspend fun selectById(id: Long): FinanceAccountEntity?

    @Query("SELECT * FROM account_period WHERE end_date IS NULL")
    suspend fun selectActivePeriods(): List<AccountPeriodEntity>

    @Query("SELECT * FROM account_period WHERE account_id = :accountId AND end_date IS NULL")
    suspend fun selectActivePeriod(accountId: Long): AccountPeriodEntity?

    @Query(
        "SELECT * FROM monthly_entry WHERE period_id IN " +
            "(SELECT id FROM account_period WHERE end_date IS NULL) ORDER BY month",
    )
    suspend fun selectActiveEntries(): List<MonthlyEntryEntity>

    @Query("SELECT * FROM monthly_entry WHERE period_id = :periodId ORDER BY month")
    suspend fun selectEntries(periodId: Long): List<MonthlyEntryEntity>
}
