package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.core.Outcome

interface AccountRepository {
    /** Stores [account] and returns it with the id the store gave it. */
    suspend fun add(account: FinanceAccount): Outcome<SavedAccount>

    /** Every stored account, oldest first. */
    suspend fun all(): Outcome<List<SavedAccount>>
}
