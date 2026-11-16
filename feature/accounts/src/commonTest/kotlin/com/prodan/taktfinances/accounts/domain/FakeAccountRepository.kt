package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.datetime.YearMonth

/** In-memory repository for use-case tests. [failing] makes every call fail. */
class FakeAccountRepository : AccountRepository {
    val accounts = mutableListOf<SavedAccount>()
    val starts = mutableListOf<StartingPointPayload>()
    val savedEntries = mutableListOf<Pair<Long, MonthlyEntry>>()
    val deletedMonths = mutableListOf<Pair<Long, YearMonth>>()
    var failing = false

    override suspend fun add(account: FinanceAccount, start: StartingPointPayload): Outcome<SavedAccount> {
        if (failing) return Outcome.Failed(Reason.Unavailable)
        val id = accounts.size + 1L
        val period = when (start) {
            is StartingPointPayload.Monthly -> Period.Monthly(id, account.createdOn, listOf(start.entry))
            is StartingPointPayload.Transactions -> Period.Transactions(id, account.createdOn, start.openingBalanceMinor)
        }
        starts += start
        return Outcome.Ok(SavedAccount(id, account, period).also { accounts += it })
    }

    override suspend fun all(): Outcome<List<SavedAccount>> =
        if (failing) Outcome.Failed(Reason.Unavailable) else Outcome.Ok(accounts.toList())

    override suspend fun get(id: Long): Outcome<SavedAccount> {
        if (failing) return Outcome.Failed(Reason.Unavailable)
        return accounts.firstOrNull { it.id == id }?.let { Outcome.Ok(it) } ?: Outcome.Failed(Reason.Unavailable)
    }

    override suspend fun saveMonthlyEntry(periodId: Long, entry: MonthlyEntry): Outcome<Unit> {
        if (failing) return Outcome.Failed(Reason.Unavailable)
        savedEntries += periodId to entry
        return Outcome.Ok(Unit)
    }

    override suspend fun deleteMonthlyEntry(periodId: Long, month: YearMonth): Outcome<Unit> {
        if (failing) return Outcome.Failed(Reason.Unavailable)
        deletedMonths += periodId to month
        return Outcome.Ok(Unit)
    }
}
