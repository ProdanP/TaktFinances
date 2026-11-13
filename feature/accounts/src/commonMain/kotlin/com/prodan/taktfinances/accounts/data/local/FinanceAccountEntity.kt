package com.prodan.taktfinances.accounts.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * One row per finance account. Detail columns are filled only for the account's type. Enum
 * columns hold stable codes owned by the data layer (see AccountRows.kt), not display titles.
 *
 * Public only because the app database in :sharedLogic lists it; it never leaves `data`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Entity(tableName = "finance_account")
class FinanceAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val name: String,
    val currency: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    val bank: String?,
    @ColumnInfo(name = "bank_kind") val bankKind: String?,
    val last4: String?,
    @ColumnInfo(name = "investment_kind") val investmentKind: String?,
    val platform: String?,
    @ColumnInfo(name = "loan_direction") val loanDirection: String?,
    val counterparty: String?,
)
