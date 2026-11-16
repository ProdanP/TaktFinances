package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.data.mapper.UnreadableRowException
import com.prodan.taktfinances.accounts.data.mapper.toDbo
import com.prodan.taktfinances.accounts.data.mapper.toDomain
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.outcomeOf
import kotlinx.datetime.YearMonth

/**
 * The DAO's suspend calls run on the database's query context, so this class does not switch threads.
 * Any store or mapping failure, and a missing account, is `Reason.Unavailable` (see [outcomeOf]).
 */
internal class AccountRepositoryImpl(
    private val dao: FinanceAccountDao,
) : AccountRepository {

    override suspend fun add(account: FinanceAccount, start: StartingPointPayload): Outcome<SavedAccount> = outcomeOf {
        val entry = (start as? StartingPointPayload.Monthly)?.entry?.toDbo(periodId = 0)
        val (accountId, periodId) = dao.insertWithStart(account.toDbo(), start.toDbo(account.createdOn), entry)
        SavedAccount(accountId, account, start.toDomain(periodId, account.createdOn))
    }

    override suspend fun all(): Outcome<List<SavedAccount>> = outcomeOf {
        val periods = dao.selectActivePeriods().associateBy { it.accountId }
        val entries = dao.selectActiveEntries().groupBy { it.periodId }
        dao.selectAll().map { row ->
            val period = periods[row.id] ?: throw UnreadableRowException("account ${row.id} has no active period")
            row.toDomain(period.toDomain(entries[period.id].orEmpty()))
        }
    }

    override suspend fun get(id: Long): Outcome<SavedAccount> = outcomeOf {
        val row = dao.selectById(id) ?: throw NoSuchElementException("account $id")
        val period = dao.selectActivePeriod(id) ?: throw UnreadableRowException("account $id has no active period")
        row.toDomain(period.toDomain(dao.selectEntries(period.id)))
    }

    override suspend fun saveMonthlyEntry(periodId: Long, entry: MonthlyEntry): Outcome<Unit> = outcomeOf {
        dao.upsertEntry(entry.toDbo(periodId))
    }

    override suspend fun deleteMonthlyEntry(periodId: Long, month: YearMonth): Outcome<Unit> = outcomeOf {
        dao.deleteEntry(periodId, month.toString())
    }
}
