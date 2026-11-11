package com.prodan.taktfinances.accounts.domain

data class FinanceAccount(
    val type: AccountType,
    val name: String,
    val currency: Currency,
    val amountMinor: Long,
)
