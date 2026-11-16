package com.prodan.taktfinances.accounts.domain.entity

/** A stored account with its active tracking [period]. [id] is unique and grows with creation order. */
data class SavedAccount(
    val id: Long,
    val account: FinanceAccount,
    val period: Period,
)
