package com.prodan.taktfinances

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/** In-memory repository for state holder tests. Entry writes change the stored period, like the real store. */
class FakeAccountRepository : AccountRepository {
    val saved = mutableListOf<SavedAccount>()
    val starts = mutableListOf<StartingPointPayload>()
    var failAdd = false
    var failAll = false
    var failGet = false
    var failSave = false
    var failDelete = false

    /** When set, [add] waits for it, so a test can observe a save in progress. */
    var addGate: CompletableDeferred<Unit>? = null

    /** When set, [saveMonthlyEntry] waits for it. */
    var saveGate: CompletableDeferred<Unit>? = null
    var addCalls = 0
        private set
    var saveCalls = 0
        private set

    val accounts: List<FinanceAccount> get() = saved.map { it.account }

    /** Stores [account] directly. A Monthly account gets [entries] (default: one entry for its creation month). */
    fun insert(
        account: FinanceAccount,
        entries: List<MonthlyEntry> = listOf(MonthlyEntry(account.createdOn.yearMonth, 100_000)),
        trades: Boolean = false,
    ): SavedAccount {
        val id = (saved.maxOfOrNull { it.id } ?: 0) + 1
        val period = if (trades) {
            Period.Transactions(id, account.createdOn, 0)
        } else {
            Period.Monthly(id, account.createdOn, entries.sortedBy { it.month })
        }
        return SavedAccount(id, account, period).also { saved += it }
    }

    fun remove(id: Long) {
        saved.removeAll { it.id == id }
    }

    override suspend fun add(account: FinanceAccount, start: StartingPointPayload): Outcome<SavedAccount> {
        addCalls++
        addGate?.await()
        if (failAdd) return Outcome.Failed(Reason.Unavailable)
        starts += start
        return Outcome.Ok(
            when (start) {
                is StartingPointPayload.Monthly -> insert(account, listOf(start.entry))
                is StartingPointPayload.Transactions -> insert(account, trades = true)
            },
        )
    }

    override suspend fun all(): Outcome<List<SavedAccount>> =
        if (failAll) Outcome.Failed(Reason.Unavailable) else Outcome.Ok(saved.toList())

    override suspend fun get(id: Long): Outcome<SavedAccount> {
        if (failGet) return Outcome.Failed(Reason.Unavailable)
        return saved.firstOrNull { it.id == id }?.let { Outcome.Ok(it) } ?: Outcome.Failed(Reason.Unavailable)
    }

    override suspend fun saveMonthlyEntry(periodId: Long, entry: MonthlyEntry): Outcome<Unit> {
        saveCalls++
        saveGate?.await()
        if (failSave) return Outcome.Failed(Reason.Unavailable)
        updateEntries(periodId) { entries -> (entries.filter { it.month != entry.month } + entry).sortedBy { it.month } }
        return Outcome.Ok(Unit)
    }

    override suspend fun deleteMonthlyEntry(periodId: Long, month: YearMonth): Outcome<Unit> {
        if (failDelete) return Outcome.Failed(Reason.Unavailable)
        updateEntries(periodId) { entries -> entries.filter { it.month != month } }
        return Outcome.Ok(Unit)
    }

    private fun updateEntries(periodId: Long, change: (List<MonthlyEntry>) -> List<MonthlyEntry>) {
        val index = saved.indexOfFirst { it.period.id == periodId }
        val account = saved[index]
        val period = account.period as Period.Monthly
        saved[index] = account.copy(period = period.copy(entries = change(period.entries)))
    }
}
