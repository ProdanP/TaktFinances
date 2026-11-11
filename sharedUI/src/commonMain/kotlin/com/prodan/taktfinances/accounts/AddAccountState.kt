package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FormCheck
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection
import com.prodan.taktfinances.accounts.domain.accountTypes
import com.prodan.taktfinances.accounts.domain.bankAccountKinds
import com.prodan.taktfinances.accounts.domain.banks
import com.prodan.taktfinances.accounts.domain.checkAccountForm
import com.prodan.taktfinances.accounts.domain.currencies
import com.prodan.taktfinances.accounts.domain.defaultCurrency
import com.prodan.taktfinances.accounts.domain.formatAmountInput
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
    /** Field that currently has input focus. */
    val focused: FormField? = null,
    /** Fields edited since they last lost focus. */
    val edited: Set<FormField> = emptySet(),
    /** Fields the user edited and then left; only these may show an error. */
    val left: Set<FormField> = emptySet(),
) {
    val check: FormCheck get() = checkAccountForm(type, name, amount, details)
    val visibleErrors: Set<FormField> get() = check.invalid intersect left
    val canSave: Boolean get() = check.canSave
    val hint: String? get() = check.hint

    /** Raw input while the amount is edited, formatted ("2,500.00") otherwise; empty when blank. */
    val amountText: String
        get() = if (focused == FormField.Amount) amount else formatAmountInput(amount).orEmpty()
}
