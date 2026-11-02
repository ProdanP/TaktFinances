package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountDraftProblem
import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency

data class AddAccountState(
    val type: AccountType? = null,
    val name: String = "",
    val currency: Currency? = null,
    val amountCanonical: String = "",
    val problems: List<AccountDraftProblem> = emptyList(),
    val unavailable: Boolean = false,
    val saving: Boolean = false,
)
