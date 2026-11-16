package com.prodan.taktfinances.accounts.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * One stretch of an account's history in one tracking mode. The active period has no [endDate].
 * Dates are ISO text ("2026-11-16"). [openingBalanceMinor] is set only for Transactions periods; a
 * Monthly period's starting balance is its start month's `monthly_entry`.
 *
 * Public only because the app database in :sharedLogic lists it; it never leaves `data`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Entity(
    tableName = "account_period",
    foreignKeys = [
        ForeignKey(
            entity = FinanceAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("account_id")],
)
class AccountPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    val mode: TrackingModeDbo,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String?,
    @ColumnInfo(name = "opening_balance_minor") val openingBalanceMinor: Long?,
)
