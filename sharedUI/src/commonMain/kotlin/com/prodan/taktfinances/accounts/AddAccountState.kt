package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.entity.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FormCheck
import com.prodan.taktfinances.accounts.domain.entity.FormField
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.accountTypes
import com.prodan.taktfinances.accounts.domain.entity.bankAccountKinds
import com.prodan.taktfinances.accounts.domain.entity.banks
import com.prodan.taktfinances.accounts.domain.entity.checkAccountForm
import com.prodan.taktfinances.accounts.domain.entity.currencies
import com.prodan.taktfinances.accounts.domain.entity.defaultCurrency
import com.prodan.taktfinances.accounts.domain.entity.formatAmountInput
import com.prodan.taktfinances.accounts.domain.entity.hasAmount
import com.prodan.taktfinances.accounts.domain.entity.investmentKinds
import com.prodan.taktfinances.accounts.domain.entity.loanDirections

internal data class AddAccountState(
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
    /** A save is in progress; a second one must not start. */
    val saving: Boolean = false,
    /** The last save could not store the account; cleared by the next edit or save. */
    val saveFailed: Boolean = false,
) {
    /** Computed once per state, not on every read of the getters below. */
    val check: FormCheck = checkAccountForm(type, name, amount, details)
    val visibleErrors: Set<FormField> get() = check.invalid intersect left
    val canSave: Boolean get() = check.canSave && !saving
    val hint: String? get() = check.hint

    /** Bank and Loan enter a balance; Investment starts at 0.00 and shows the tracking notice instead. */
    val showsAmount: Boolean get() = type.hasAmount

    /** Raw input while the amount is edited, formatted ("2,500.00") otherwise; empty when blank. */
    val amountText: String
        get() = if (focused == FormField.Amount) amount else formatAmountInput(amount).orEmpty()
}
