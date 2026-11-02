package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency

enum class AccountDraftProblem {
    BlankName,
    MissingType,
    MissingCurrency,
    MissingAmount,
}

fun accountDraftProblems(
    name: String,
    type: AccountType?,
    currency: Currency?,
    minorUnits: Long?,
): List<AccountDraftProblem> = buildList {
    if (name.trim().isEmpty()) add(AccountDraftProblem.BlankName)
    if (type == null) add(AccountDraftProblem.MissingType)
    if (currency == null) add(AccountDraftProblem.MissingCurrency)
    if (minorUnits == null) add(AccountDraftProblem.MissingAmount)
}
