package com.prodan.taktfinances

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import com.prodan.taktfinances.accounts.domain.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.CompletableDeferred

/** In-memory repository for state holder tests. */
class FakeAccountRepository(initial: List<FinanceAccount> = emptyList()) : AccountRepository {
    val saved = mutableListOf<SavedAccount>()
    var failAdd = false
    var failAll = false
    /** When set, [add] waits for it, so a test can observe a save in progress. */
    var addGate: CompletableDeferred<Unit>? = null
    var addCalls = 0
        private set

    init {
        initial.forEach { saved += SavedAccount(saved.size + 1L, it) }
    }

    val accounts: List<FinanceAccount> get() = saved.map { it.account }

    fun insert(account: FinanceAccount): SavedAccount =
        SavedAccount((saved.maxOfOrNull { it.id } ?: 0) + 1, account).also { saved += it }

    fun remove(id: Long) {
        saved.removeAll { it.id == id }
    }

    override suspend fun add(account: FinanceAccount): Outcome<SavedAccount> {
        addCalls++
        addGate?.await()
        if (failAdd) return Outcome.Failed(Reason.Unavailable)
        return Outcome.Ok(insert(account))
    }

    override suspend fun all(): Outcome<List<SavedAccount>> =
        if (failAll) Outcome.Failed(Reason.Unavailable) else Outcome.Ok(saved.toList())
}
