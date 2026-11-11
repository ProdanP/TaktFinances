package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountSummaryTest {
    @Test
    fun bankSummaryIsKindThenBank() {
        val details = AccountDetailsDraft(bank = Bank.Victoriabank, bankAccountKind = BankAccountKind.Savings)
        assertEquals("Savings · Victoriabank", accountSummary(AccountType.BankAccount, details))
        assertEquals("MAIB", accountSummary(AccountType.BankAccount, AccountDetailsDraft(bank = Bank.Maib)))
    }

    @Test
    fun investmentAndLoanSummaries() {
        val investment = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = " Binance ")
        assertEquals("Crypto · Binance", accountSummary(AccountType.Investment, investment))
        val loan = AccountDetailsDraft(loanDirection = LoanDirection.Lent, counterparty = "Ana")
        assertEquals("Lent · Ana", accountSummary(AccountType.Loan, loan))
    }

    @Test
    fun emptyOrOtherTypeDetailsGiveNoSummary() {
        assertNull(accountSummary(AccountType.BankAccount, AccountDetailsDraft()))
        assertNull(accountSummary(AccountType.Loan, AccountDetailsDraft(bank = Bank.Maib, platform = "X")))
    }
}
