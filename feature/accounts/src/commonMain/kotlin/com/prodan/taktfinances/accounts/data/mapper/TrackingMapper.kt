package com.prodan.taktfinances.accounts.data.mapper

import com.prodan.taktfinances.accounts.data.local.AccountPeriodEntity
import com.prodan.taktfinances.accounts.data.local.MonthlyEntryEntity
import com.prodan.taktfinances.accounts.data.local.TrackingModeDbo
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** The first period of an account created on [createdOn]; ids are filled in by the insert. */
internal fun StartingPointPayload.toDbo(createdOn: LocalDate): AccountPeriodEntity = when (this) {
    is StartingPointPayload.Monthly -> AccountPeriodEntity(0, 0, TrackingModeDbo.MONTHLY, createdOn.toString(), null, null)
    is StartingPointPayload.Transactions ->
        AccountPeriodEntity(0, 0, TrackingModeDbo.TRANSACTIONS, createdOn.toString(), null, openingBalanceMinor)
}

/** The period a newly stored [StartingPointPayload] became. */
internal fun StartingPointPayload.toDomain(periodId: Long, createdOn: LocalDate): Period = when (this) {
    is StartingPointPayload.Monthly -> Period.Monthly(periodId, createdOn, listOf(entry))
    is StartingPointPayload.Transactions -> Period.Transactions(periodId, createdOn, openingBalanceMinor)
}

internal fun MonthlyEntry.toDbo(periodId: Long): MonthlyEntryEntity =
    MonthlyEntryEntity(periodId, month.toString(), amountMinor)

/** [entries] are this period's rows; a malformed date or a Transactions period without an opening balance fails. */
internal fun AccountPeriodEntity.toDomain(entries: List<MonthlyEntryEntity>): Period {
    val start = LocalDate.parse(startDate)
    return when (mode) {
        TrackingModeDbo.MONTHLY -> Period.Monthly(id, start, entries.map { it.toDomain() }.sortedBy { it.month })
        TrackingModeDbo.TRANSACTIONS -> Period.Transactions(
            id,
            start,
            openingBalanceMinor ?: throw UnreadableRowException("period $id has no opening_balance_minor"),
        )
    }
}

internal fun MonthlyEntryEntity.toDomain(): MonthlyEntry = MonthlyEntry(YearMonth.parse(month), amountMinor)
