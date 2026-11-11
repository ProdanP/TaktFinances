package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.FinanceAccount

internal class AccountRepositoryImpl : AccountRepository {
    private val accounts = mutableListOf<FinanceAccount>()

    override fun add(account: FinanceAccount) {
        accounts += account
    }

    override fun all(): List<FinanceAccount> = accounts.toList()
}
