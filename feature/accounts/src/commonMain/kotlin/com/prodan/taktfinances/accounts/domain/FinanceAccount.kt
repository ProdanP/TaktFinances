package com.prodan.taktfinances.accounts.domain

data class FinanceAccount(
    val name: String,
    val currency: Currency,
    val amountMinor: Long,
    val details: AccountDetails,
) {
    val type: AccountType get() = details.type
}
