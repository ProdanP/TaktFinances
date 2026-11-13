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

/** The same summary for a saved account, so its card reads like the Add Account preview. */
fun accountSummary(details: AccountDetails): String = accountSummary(details.type, details.toDraft())!!

private fun AccountDetails.toDraft(): AccountDetailsDraft = when (this) {
    is AccountDetails.BankDetails -> AccountDetailsDraft(bank = bank, bankAccountKind = kind, last4 = last4.orEmpty())
    is AccountDetails.InvestmentDetails -> AccountDetailsDraft(investmentKind = kind, platform = platform)
    is AccountDetails.LoanDetails -> AccountDetailsDraft(loanDirection = direction, counterparty = counterparty)
}
