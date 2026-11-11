package com.prodan.taktfinances.accounts.domain

sealed interface AddAccountResult {
    data class Created(val account: FinanceAccount) : AddAccountResult
    data class Invalid(val invalid: Set<FormField>) : AddAccountResult
}

class AddAccount(private val repository: AccountRepository) {
    operator fun invoke(
        type: AccountType,
        name: String,
        currency: Currency,
        amount: String,
        details: AccountDetailsDraft = AccountDetailsDraft(),
    ): AddAccountResult {
        val check = checkAccountForm(type, name, amount, details)
        if (!check.canSave) return AddAccountResult.Invalid(check.invalid)
        val account = FinanceAccount(
            name = name.trim(),
            currency = currency,
            amountMinor = parseAmountMinor(amount)!!,
            details = buildDetails(type, details),
        )
        repository.add(account)
        return AddAccountResult.Created(account)
    }
}

/** Builds the details of [type]; call only after [checkAccountForm] reported no invalid fields. */
private fun buildDetails(type: AccountType, draft: AccountDetailsDraft): AccountDetails = when (type) {
    AccountType.BankAccount -> AccountDetails.BankDetails(
        bank = draft.bank!!,
        kind = draft.bankAccountKind!!,
        last4 = draft.last4.trim().ifEmpty { null },
    )
    AccountType.Investment -> AccountDetails.InvestmentDetails(
        kind = draft.investmentKind!!,
        platform = draft.platform.trim(),
    )
    AccountType.Loan -> AccountDetails.LoanDetails(
        direction = draft.loanDirection!!,
        counterparty = draft.counterparty.trim(),
    )
}
