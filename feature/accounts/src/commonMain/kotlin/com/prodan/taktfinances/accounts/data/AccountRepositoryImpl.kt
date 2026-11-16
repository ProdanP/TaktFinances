package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.data.mapper.toDbo
import com.prodan.taktfinances.accounts.data.mapper.toDomain
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.outcomeOf

/**
 * The DAO's suspend calls run on the database's query context, so this class does not switch threads.
 * Any store or mapping failure is `Reason.Unavailable` (see [outcomeOf]).
 */
internal class AccountRepositoryImpl(
    private val dao: FinanceAccountDao,
) : AccountRepository {

    override suspend fun add(account: FinanceAccount): Outcome<SavedAccount> = outcomeOf {
        SavedAccount(dao.insert(account.toDbo()), account)
    }

    override suspend fun all(): Outcome<List<SavedAccount>> = outcomeOf {
        dao.selectAll().map { it.toDomain() }
    }
}
