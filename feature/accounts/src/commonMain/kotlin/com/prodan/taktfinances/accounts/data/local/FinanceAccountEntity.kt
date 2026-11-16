package com.prodan.taktfinances.accounts.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * One row per finance account. Detail columns are filled only for the account's type. Enum
 * columns use this package's storage enums (`*Dbo`), not the domain enums. [createdOn] is ISO text
 * ("2026-11-16"). The balance is not stored here; it comes from the account's period.
 *
 * Public only because the app database in :sharedLogic lists it; it never leaves `data`.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
@Entity(tableName = "finance_account")
class FinanceAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: AccountTypeDbo,
    val name: String,
    val currency: CurrencyDbo,
    @ColumnInfo(name = "created_on") val createdOn: String,
    val bank: BankDbo?,
    @ColumnInfo(name = "bank_kind") val bankKind: BankKindDbo?,
    val last4: String?,
    @ColumnInfo(name = "investment_kind") val investmentKind: InvestmentKindDbo?,
    val platform: String?,
    @ColumnInfo(name = "loan_direction") val loanDirection: LoanDirectionDbo?,
    val counterparty: String?,
)
