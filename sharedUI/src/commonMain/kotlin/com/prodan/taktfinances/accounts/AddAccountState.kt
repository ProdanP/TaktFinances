package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.DetailErrors
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection
import com.prodan.taktfinances.accounts.domain.accountTypes
import com.prodan.taktfinances.accounts.domain.bankAccountKinds
import com.prodan.taktfinances.accounts.domain.banks
import com.prodan.taktfinances.accounts.domain.currencies
import com.prodan.taktfinances.accounts.domain.defaultCurrency
import com.prodan.taktfinances.accounts.domain.investmentKinds
import com.prodan.taktfinances.accounts.domain.loanDirections

data class AddAccountState(
    val types: List<AccountType> = accountTypes(),
    val currencies: List<Currency> = currencies(),
    val banks: List<Bank> = banks(),
    val bankAccountKinds: List<BankAccountKind> = bankAccountKinds(),
    val investmentKinds: List<InvestmentKind> = investmentKinds(),
    val loanDirections: List<LoanDirection> = loanDirections(),
    val type: AccountType = AccountType.BankAccount,
    val currency: Currency = defaultCurrency(),
    val name: String = "",
    val amount: String = "",
    val details: AccountDetailsDraft = AccountDetailsDraft(),
    val nameInvalid: Boolean = false,
    val amountInvalid: Boolean = false,
    val detailErrors: DetailErrors = DetailErrors(),
)
