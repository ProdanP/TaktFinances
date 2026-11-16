package com.prodan.taktfinances.accounts.domain.entity

enum class AccountType(val title: String) {
    BankAccount("Bank"),
    Investment("Investment"),
    Loan("Loan"),
}

fun accountTypes(): List<AccountType> = AccountType.entries

/** Bank and Loan start from an entered balance; Investment starts at 0.00 and is tracked by trades. */
val AccountType.hasAmount: Boolean get() = this != AccountType.Investment
