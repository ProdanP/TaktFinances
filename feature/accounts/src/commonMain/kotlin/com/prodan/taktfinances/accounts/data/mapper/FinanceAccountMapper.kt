package com.prodan.taktfinances.accounts.data.mapper

import com.prodan.taktfinances.accounts.data.local.AccountTypeDbo
import com.prodan.taktfinances.accounts.data.local.BankDbo
import com.prodan.taktfinances.accounts.data.local.BankKindDbo
import com.prodan.taktfinances.accounts.data.local.CurrencyDbo
import com.prodan.taktfinances.accounts.data.local.FinanceAccountEntity
import com.prodan.taktfinances.accounts.data.local.InvestmentKindDbo
import com.prodan.taktfinances.accounts.data.local.LoanDirectionDbo
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount

/** A row that does not map back to the domain (missing detail for its type) is a read failure. */
internal class UnreadableRowException(message: String) : Exception(message)

internal fun FinanceAccount.toDbo(): FinanceAccountEntity {
    val bank = details as? AccountDetails.BankDetails
    val investment = details as? AccountDetails.InvestmentDetails
    val loan = details as? AccountDetails.LoanDetails
    return FinanceAccountEntity(
        id = 0,
        type = type.toDbo(),
        name = name,
        currency = currency.toDbo(),
        amountMinor = amountMinor,
        bank = bank?.bank?.toDbo(),
        bankKind = bank?.kind?.toDbo(),
        last4 = bank?.last4,
        investmentKind = investment?.kind?.toDbo(),
        platform = investment?.platform,
        loanDirection = loan?.direction?.toDbo(),
        counterparty = loan?.counterparty,
    )
}

internal fun FinanceAccountEntity.toDomain(): SavedAccount {
    val details = when (type) {
        AccountTypeDbo.BANK -> AccountDetails.BankDetails(
            bank = required(bank, "bank").toDomain(),
            kind = required(bankKind, "bank_kind").toDomain(),
            last4 = last4,
        )
        AccountTypeDbo.INVESTMENT -> AccountDetails.InvestmentDetails(
            kind = required(investmentKind, "investment_kind").toDomain(),
            platform = required(platform, "platform"),
        )
        AccountTypeDbo.LOAN -> AccountDetails.LoanDetails(
            direction = required(loanDirection, "loan_direction").toDomain(),
            counterparty = required(counterparty, "counterparty"),
        )
    }
    val account = FinanceAccount(
        name = name,
        currency = currency.toDomain(),
        amountMinor = amountMinor,
        details = details,
    )
    return SavedAccount(id, account)
}

private fun <T : Any> FinanceAccountEntity.required(value: T?, column: String): T =
    value ?: throw UnreadableRowException("account $id has no $column")

private fun AccountType.toDbo(): AccountTypeDbo = when (this) {
    AccountType.BankAccount -> AccountTypeDbo.BANK
    AccountType.Investment -> AccountTypeDbo.INVESTMENT
    AccountType.Loan -> AccountTypeDbo.LOAN
}

private fun Currency.toDbo(): CurrencyDbo = when (this) {
    Currency.MDL -> CurrencyDbo.MDL
    Currency.USD -> CurrencyDbo.USD
    Currency.EUR -> CurrencyDbo.EUR
    Currency.GBP -> CurrencyDbo.GBP
    Currency.RON -> CurrencyDbo.RON
}

private fun CurrencyDbo.toDomain(): Currency = when (this) {
    CurrencyDbo.MDL -> Currency.MDL
    CurrencyDbo.USD -> Currency.USD
    CurrencyDbo.EUR -> Currency.EUR
    CurrencyDbo.GBP -> Currency.GBP
    CurrencyDbo.RON -> Currency.RON
}

private fun Bank.toDbo(): BankDbo = when (this) {
    Bank.Maib -> BankDbo.MAIB
    Bank.Victoriabank -> BankDbo.VICTORIABANK
    Bank.Micb -> BankDbo.MICB
    Bank.ProCredit -> BankDbo.PROCREDIT
    Bank.Mobiasbanca -> BankDbo.MOBIASBANCA
    Bank.Other -> BankDbo.OTHER
}

private fun BankDbo.toDomain(): Bank = when (this) {
    BankDbo.MAIB -> Bank.Maib
    BankDbo.VICTORIABANK -> Bank.Victoriabank
    BankDbo.MICB -> Bank.Micb
    BankDbo.PROCREDIT -> Bank.ProCredit
    BankDbo.MOBIASBANCA -> Bank.Mobiasbanca
    BankDbo.OTHER -> Bank.Other
}

private fun BankAccountKind.toDbo(): BankKindDbo = when (this) {
    BankAccountKind.Current -> BankKindDbo.CURRENT
    BankAccountKind.Savings -> BankKindDbo.SAVINGS
    BankAccountKind.Card -> BankKindDbo.CARD
    BankAccountKind.Deposit -> BankKindDbo.DEPOSIT
}

private fun BankKindDbo.toDomain(): BankAccountKind = when (this) {
    BankKindDbo.CURRENT -> BankAccountKind.Current
    BankKindDbo.SAVINGS -> BankAccountKind.Savings
    BankKindDbo.CARD -> BankAccountKind.Card
    BankKindDbo.DEPOSIT -> BankAccountKind.Deposit
}

private fun InvestmentKind.toDbo(): InvestmentKindDbo = when (this) {
    InvestmentKind.StocksEtf -> InvestmentKindDbo.STOCKS_ETF
    InvestmentKind.Crypto -> InvestmentKindDbo.CRYPTO
    InvestmentKind.Bonds -> InvestmentKindDbo.BONDS
    InvestmentKind.Deposit -> InvestmentKindDbo.DEPOSIT
    InvestmentKind.RealEstate -> InvestmentKindDbo.REAL_ESTATE
    InvestmentKind.Other -> InvestmentKindDbo.OTHER
}

private fun InvestmentKindDbo.toDomain(): InvestmentKind = when (this) {
    InvestmentKindDbo.STOCKS_ETF -> InvestmentKind.StocksEtf
    InvestmentKindDbo.CRYPTO -> InvestmentKind.Crypto
    InvestmentKindDbo.BONDS -> InvestmentKind.Bonds
    InvestmentKindDbo.DEPOSIT -> InvestmentKind.Deposit
    InvestmentKindDbo.REAL_ESTATE -> InvestmentKind.RealEstate
    InvestmentKindDbo.OTHER -> InvestmentKind.Other
}

private fun LoanDirection.toDbo(): LoanDirectionDbo = when (this) {
    LoanDirection.Borrowed -> LoanDirectionDbo.BORROWED
    LoanDirection.Lent -> LoanDirectionDbo.LENT
}

private fun LoanDirectionDbo.toDomain(): LoanDirection = when (this) {
    LoanDirectionDbo.BORROWED -> LoanDirection.Borrowed
    LoanDirectionDbo.LENT -> LoanDirection.Lent
}
