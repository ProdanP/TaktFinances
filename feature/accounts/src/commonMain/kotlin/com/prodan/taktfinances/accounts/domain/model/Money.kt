package com.prodan.taktfinances.accounts.domain.model

data class Money(
    val minorUnits: Long,
    val currency: Currency,
)
