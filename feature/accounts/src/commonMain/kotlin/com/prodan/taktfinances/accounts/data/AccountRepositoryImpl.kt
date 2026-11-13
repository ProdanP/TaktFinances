package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import com.prodan.taktfinances.accounts.domain.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.CancellationException

/** The DAO's suspend calls run on the database's query context, so this class does not switch threads. */
internal class AccountRepositoryImpl(
    private val dao: FinanceAccountDao,
) : AccountRepository {

    override suspend fun add(account: FinanceAccount): Outcome<SavedAccount> = storeCall {
        SavedAccount(dao.insert(account.toRow()), account)
    }

    override suspend fun all(): Outcome<List<SavedAccount>> = storeCall {
        dao.selectAll().map { it.toSavedAccount() }
    }

    /** Any store or mapping failure is [Reason.Unavailable]. */
    private suspend fun <T : Any> storeCall(block: suspend () -> T): Outcome<T> = try {
        Outcome.Ok(block())
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Outcome.Failed(Reason.Unavailable)
    }
}
