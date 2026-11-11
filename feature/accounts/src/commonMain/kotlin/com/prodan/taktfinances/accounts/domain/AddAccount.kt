package com.prodan.taktfinances.accounts.domain

sealed interface AddAccountResult {
    data class Created(val account: FinanceAccount) : AddAccountResult
    data class Invalid(
        val nameInvalid: Boolean,
        val amountInvalid: Boolean,
        val detailErrors: DetailErrors = DetailErrors(),
    ) : AddAccountResult
}

class AddAccount(private val repository: AccountRepository) {
    operator fun invoke(
        type: AccountType,
        name: String,
        currency: Currency,
        amount: String,
        details: AccountDetailsDraft = AccountDetailsDraft(),
    ): AddAccountResult {
        val trimmedName = name.trim()
        val amountMinor = parseAmountMinor(amount, currency)
        val (accountDetails, errors) = buildDetails(type, details)
        if (trimmedName.isEmpty() || amountMinor == null || accountDetails == null) {
            return AddAccountResult.Invalid(
                nameInvalid = trimmedName.isEmpty(),
                amountInvalid = amountMinor == null,
                detailErrors = errors,
            )
        }
        val account = FinanceAccount(trimmedName, currency, amountMinor, accountDetails)
        repository.add(account)
        return AddAccountResult.Created(account)
    }
}

private fun buildDetails(
    type: AccountType,
    draft: AccountDetailsDraft,
): Pair<AccountDetails?, DetailErrors> = when (type) {
    AccountType.BankAccount -> {
        val last4 = draft.last4.trim()
        val errors = DetailErrors(
            bank = draft.bank == null,
            accountKind = draft.bankAccountKind == null,
            last4 = last4.isNotEmpty() && !LAST4.matches(last4),
        )
        val details = if (errors.any) null else AccountDetails.BankDetails(
            bank = draft.bank!!,
            kind = draft.bankAccountKind!!,
            last4 = last4.ifEmpty { null },
        )
        details to errors
    }
    AccountType.Investment -> {
        val platform = draft.platform.trim()
        val errors = DetailErrors(
            investmentKind = draft.investmentKind == null,
            platform = platform.isEmpty(),
        )
        val details = if (errors.any) null else AccountDetails.InvestmentDetails(
            kind = draft.investmentKind!!,
            platform = platform,
        )
        details to errors
    }
    AccountType.Loan -> {
        val counterparty = draft.counterparty.trim()
        val errors = DetailErrors(
            direction = draft.loanDirection == null,
            counterparty = counterparty.isEmpty(),
        )
        val details = if (errors.any) null else AccountDetails.LoanDetails(
            direction = draft.loanDirection!!,
            counterparty = counterparty,
        )
        details to errors
    }
}

private val LAST4 = Regex("""\d{4}""")

internal fun parseAmountMinor(text: String, currency: Currency): Long? {
    val digits = currency.minorUnitDigits
    val pattern = if (digits == 0) Regex("""\d+""") else Regex("""\d+(\.\d{1,$digits})?""")
    val value = text.trim()
    if (!pattern.matches(value)) return null
    val whole = value.substringBefore('.')
    val fraction = value.substringAfter('.', "").padEnd(digits, '0')
    return (whole + fraction).toLongOrNull()
}
