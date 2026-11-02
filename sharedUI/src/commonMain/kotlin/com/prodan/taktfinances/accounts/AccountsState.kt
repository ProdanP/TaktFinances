package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.model.Account

data class AccountsState(
    val loading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val unavailable: Boolean = false,
)
