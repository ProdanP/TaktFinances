package com.prodan.taktfinances.accounts.data.mapper

import com.prodan.taktfinances.accounts.data.local.FinanceAccountEntity
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount

// Stored codes. They are part of the schema: renaming an enum entry or its title must not change them.
private val TYPE_CODES = mapOf(AccountType.BankAccount to "BANK", AccountType.Investment to "INVESTMENT", AccountType.Loan to "LOAN")
private val CURRENCY_CODES = Currency.entries.associateWith { it.code }
private val BANK_CODES = mapOf(
    Bank.Maib to "MAIB",
    Bank.Victoriabank to "VICTORIABANK",
    Bank.Micb to "MICB",
    Bank.ProCredit to "PROCREDIT",
    Bank.Mobiasbanca to "MOBIASBANCA",
    Bank.Other to "OTHER",
)
private val BANK_KIND_CODES = mapOf(
    BankAccountKind.Current to "CURRENT",
    BankAccountKind.Savings to "SAVINGS",
    BankAccountKind.Card to "CARD",
    BankAccountKind.Deposit to "DEPOSIT",
)
private val INVESTMENT_KIND_CODES = mapOf(
    InvestmentKind.StocksEtf to "STOCKS_ETF",
    InvestmentKind.Crypto to "CRYPTO",
    InvestmentKind.Bonds to "BONDS",
    InvestmentKind.Deposit to "DEPOSIT",
    InvestmentKind.RealEstate to "REAL_ESTATE",
    InvestmentKind.Other to "OTHER",
)
private val LOAN_DIRECTION_CODES = mapOf(LoanDirection.Borrowed to "BORROWED", LoanDirection.Lent to "LENT")

/** A row that does not map back to the domain (unknown code, missing detail) is a read failure. */
internal class UnreadableRowException(message: String) : Exception(message)

internal fun FinanceAccount.toDbo(): FinanceAccountEntity {
    val bank = details as? AccountDetails.BankDetails
    val investment = details as? AccountDetails.InvestmentDetails
    val loan = details as? AccountDetails.LoanDetails
    return FinanceAccountEntity(
        id = 0,
        type = TYPE_CODES.getValue(type),
        name = name,
        currency = CURRENCY_CODES.getValue(currency),
        amountMinor = amountMinor,
        bank = bank?.let { BANK_CODES.getValue(it.bank) },
        bankKind = bank?.let { BANK_KIND_CODES.getValue(it.kind) },
        last4 = bank?.last4,
        investmentKind = investment?.let { INVESTMENT_KIND_CODES.getValue(it.kind) },
        platform = investment?.platform,
        loanDirection = loan?.let { LOAN_DIRECTION_CODES.getValue(it.direction) },
        counterparty = loan?.counterparty,
    )
}

internal fun FinanceAccountEntity.toDomain(): SavedAccount {
    val details = when (TYPE_CODES.decode(type, "type")) {
        AccountType.BankAccount -> AccountDetails.BankDetails(
            bank = BANK_CODES.decode(bank, "bank"),
            kind = BANK_KIND_CODES.decode(bankKind, "bank_kind"),
            last4 = last4,
        )
        AccountType.Investment -> AccountDetails.InvestmentDetails(
            kind = INVESTMENT_KIND_CODES.decode(investmentKind, "investment_kind"),
            platform = platform ?: throw UnreadableRowException("account $id has no platform"),
        )
        AccountType.Loan -> AccountDetails.LoanDetails(
            direction = LOAN_DIRECTION_CODES.decode(loanDirection, "loan_direction"),
            counterparty = counterparty ?: throw UnreadableRowException("account $id has no counterparty"),
        )
    }
    val account = FinanceAccount(
        name = name,
        currency = CURRENCY_CODES.decode(currency, "currency"),
        amountMinor = amountMinor,
        details = details,
    )
    return SavedAccount(id, account)
}

private fun <T> Map<T, String>.decode(code: String?, column: String): T =
    entries.firstOrNull { it.value == code }?.key ?: throw UnreadableRowException("unknown $column code: $code")
