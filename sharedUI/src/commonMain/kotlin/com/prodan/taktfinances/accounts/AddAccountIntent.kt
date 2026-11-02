package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency

sealed interface AddAccountIntent {
    data class TypeSelected(val type: AccountType) : AddAccountIntent
    data class NameChanged(val value: String) : AddAccountIntent
    data class CurrencySelected(val currency: Currency) : AddAccountIntent
    data class AmountChanged(val value: String) : AddAccountIntent
    data object SaveTapped : AddAccountIntent
}
