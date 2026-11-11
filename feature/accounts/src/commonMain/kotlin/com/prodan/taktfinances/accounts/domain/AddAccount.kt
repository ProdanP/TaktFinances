package com.prodan.taktfinances.accounts.domain

sealed interface AddAccountResult {
    data class Created(val account: FinanceAccount) : AddAccountResult
    data class Invalid(val nameInvalid: Boolean, val amountInvalid: Boolean) : AddAccountResult
}

class AddAccount(private val repository: AccountRepository) {
    operator fun invoke(
        type: AccountType,
        name: String,
        currency: Currency,
        amount: String,
    ): AddAccountResult {
        val trimmedName = name.trim()
        val amountMinor = parseAmountMinor(amount, currency)
        if (trimmedName.isEmpty() || amountMinor == null) {
            return AddAccountResult.Invalid(
                nameInvalid = trimmedName.isEmpty(),
                amountInvalid = amountMinor == null,
            )
        }
        val account = FinanceAccount(type, trimmedName, currency, amountMinor)
        repository.add(account)
        return AddAccountResult.Created(account)
    }
}

internal fun parseAmountMinor(text: String, currency: Currency): Long? {
    val digits = currency.minorUnitDigits
    val pattern = if (digits == 0) Regex("""\d+""") else Regex("""\d+(\.\d{1,$digits})?""")
    val value = text.trim()
    if (!pattern.matches(value)) return null
    val whole = value.substringBefore('.')
    val fraction = value.substringAfter('.', "").padEnd(digits, '0')
    return (whole + fraction).toLongOrNull()
}
