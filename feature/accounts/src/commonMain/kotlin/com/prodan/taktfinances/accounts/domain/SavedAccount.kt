package com.prodan.taktfinances.accounts.domain

/** A stored account. [id] is unique and grows with creation order. */
data class SavedAccount(
    val id: Long,
    val account: FinanceAccount,
)
