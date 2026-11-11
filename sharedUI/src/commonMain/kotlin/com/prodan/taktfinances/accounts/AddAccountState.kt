package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.accountTypes
import com.prodan.taktfinances.accounts.domain.currencies
import com.prodan.taktfinances.accounts.domain.defaultCurrency

data class AddAccountState(
    val types: List<AccountType> = accountTypes(),
    val currencies: List<Currency> = currencies(),
    val type: AccountType = AccountType.BankAccount,
    val currency: Currency = defaultCurrency(),
    val name: String = "",
    val amount: String = "",
    val nameInvalid: Boolean = false,
    val amountInvalid: Boolean = false,
)
