package com.prodan.taktfinances.accounts.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * The balance of a Monthly period for one month ("2026-11"); at most one row per period and month.
 *
 * Public only because the app database in :sharedLogic lists it; it never leaves `data`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Entity(
    tableName = "monthly_entry",
    primaryKeys = ["period_id", "month"],
    foreignKeys = [
        ForeignKey(
            entity = AccountPeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["period_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
class MonthlyEntryEntity(
    @ColumnInfo(name = "period_id") val periodId: Long,
    val month: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
)
