package com.prodan.taktfinances.accounts.domain

interface AccountRepository {
    fun add(account: FinanceAccount)
    fun all(): List<FinanceAccount>
}
