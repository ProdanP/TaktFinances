package com.prodan.taktfinances.accounts.domain.entity

enum class AccountType(val title: String) {
    BankAccount("Bank"),
    Investment("Investment"),
    Loan("Loan"),
}

fun accountTypes(): List<AccountType> = AccountType.entries
