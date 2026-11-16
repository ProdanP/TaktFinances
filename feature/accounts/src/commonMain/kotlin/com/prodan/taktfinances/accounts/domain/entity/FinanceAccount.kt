package com.prodan.taktfinances.accounts.domain.entity

import kotlinx.datetime.LocalDate

/** [createdOn] never changes; entries can't be dated before its month. The balance comes from the period. */
data class FinanceAccount(
    val name: String,
    val currency: Currency,
    val details: AccountDetails,
    val createdOn: LocalDate,
) {
    val type: AccountType get() = details.type
}
