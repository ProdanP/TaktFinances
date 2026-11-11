package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection

sealed interface AddAccountIntent {
    data class TypeSelected(val type: AccountType) : AddAccountIntent
    data class CurrencySelected(val currency: Currency) : AddAccountIntent
    data class NameChanged(val value: String) : AddAccountIntent
    data class AmountChanged(val value: String) : AddAccountIntent
    data class BankSelected(val bank: Bank) : AddAccountIntent
    data class BankAccountKindSelected(val kind: BankAccountKind) : AddAccountIntent
    data class Last4Changed(val value: String) : AddAccountIntent
    data class InvestmentKindSelected(val kind: InvestmentKind) : AddAccountIntent
    data class PlatformChanged(val value: String) : AddAccountIntent
    data class LoanDirectionSelected(val direction: LoanDirection) : AddAccountIntent
    data class CounterpartyChanged(val value: String) : AddAccountIntent
    data class FieldFocusChanged(val field: FormField, val focused: Boolean) : AddAccountIntent
    data object SaveTapped : AddAccountIntent
}
