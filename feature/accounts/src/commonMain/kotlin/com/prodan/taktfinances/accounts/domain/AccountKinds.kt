package com.prodan.taktfinances.accounts.domain

enum class BankAccountKind(val title: String) {
    Current("Current"),
    Savings("Savings"),
    Card("Card"),
    Deposit("Deposit"),
}

fun bankAccountKinds(): List<BankAccountKind> = BankAccountKind.entries

enum class InvestmentKind(val title: String) {
    StocksEtf("Stocks/ETF"),
    Crypto("Crypto"),
    Bonds("Bonds"),
    Deposit("Deposit"),
    RealEstate("Real estate"),
    Other("Other"),
}

fun investmentKinds(): List<InvestmentKind> = InvestmentKind.entries

enum class LoanDirection(val title: String) {
    Borrowed("Borrowed"),
    Lent("Lent"),
}

fun loanDirections(): List<LoanDirection> = LoanDirection.entries
