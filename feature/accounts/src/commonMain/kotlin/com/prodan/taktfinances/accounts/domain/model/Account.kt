package com.prodan.taktfinances.accounts.domain.model

data class Account(
    val id: AccountId,
    val name: String,
    val type: AccountType,
    val balance: Money,
)

data class NewAccount(
    val name: String,
    val type: AccountType,
    val balance: Money,
)
