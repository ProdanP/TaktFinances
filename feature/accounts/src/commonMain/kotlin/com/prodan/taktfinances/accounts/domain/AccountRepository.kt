package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.accounts.domain.model.Account
import com.prodan.taktfinances.accounts.domain.model.NewAccount
import com.prodan.taktfinances.core.Outcome

interface AccountRepository {
    suspend fun accounts(): Outcome<List<Account>>
    suspend fun add(account: NewAccount): Outcome<Account>
}
