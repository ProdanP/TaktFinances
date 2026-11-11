package com.prodan.taktfinances.accounts.domain

/**
 * One-line summary of the chosen details for the account preview, e.g. "Savings · Victoriabank"
 * or "Crypto · Binance". Returns null when nothing of the type has been chosen yet.
 */
fun accountSummary(type: AccountType, details: AccountDetailsDraft): String? {
    val parts = when (type) {
        AccountType.BankAccount -> listOfNotNull(details.bankAccountKind?.title, details.bank?.shortName)
        AccountType.Investment -> listOfNotNull(details.investmentKind?.title, details.platform.trim().ifEmpty { null })
        AccountType.Loan -> listOfNotNull(details.loanDirection?.title, details.counterparty.trim().ifEmpty { null })
    }
    return parts.joinToString(" · ").ifEmpty { null }
}
