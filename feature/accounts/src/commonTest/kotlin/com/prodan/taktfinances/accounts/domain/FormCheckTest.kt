package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormCheckTest {
    private val empty = AccountDetailsDraft()
    private val bank = AccountDetailsDraft(bank = Bank.Maib, bankAccountKind = BankAccountKind.Savings)
    private val investment = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = "Binance")
    private val loan = AccountDetailsDraft(loanDirection = LoanDirection.Lent, counterparty = "Ana")

    @Test
    fun emptyFormListsTheFieldsOfEachTypeInOrder() {
        assertEquals(
            listOf(FormField.Name, FormField.Amount, FormField.Bank, FormField.AccountKind),
            checkAccountForm(AccountType.BankAccount, "", "", empty).missing,
        )
        assertEquals(
            listOf(FormField.Name, FormField.Amount, FormField.InvestmentKind, FormField.Platform),
            checkAccountForm(AccountType.Investment, "", "", empty).missing,
        )
        assertEquals(
            listOf(FormField.Name, FormField.Amount, FormField.Direction, FormField.Counterparty),
            checkAccountForm(AccountType.Loan, " ", "", empty).missing,
        )
        assertFalse(checkAccountForm(AccountType.Loan, "", "", empty).canSave)
    }

    @Test
    fun completeFormCanBeSaved() {
        assertTrue(checkAccountForm(AccountType.BankAccount, "Salary", "2500", bank).canSave)
        assertTrue(checkAccountForm(AccountType.Investment, "Fund", "12.", investment).canSave)
        assertTrue(checkAccountForm(AccountType.Loan, "Car", "0", loan).canSave)
    }

    @Test
    fun last4IsOptionalButMustBeFourDigits() {
        assertTrue(checkAccountForm(AccountType.BankAccount, "Main", "1", bank.copy(last4 = "")).canSave)
        assertTrue(checkAccountForm(AccountType.BankAccount, "Main", "1", bank.copy(last4 = "0421")).canSave)
        for (value in listOf("12", "123", "12a4")) {
            assertEquals(
                setOf(FormField.Last4),
                checkAccountForm(AccountType.BankAccount, "Main", "1", bank.copy(last4 = value)).invalid,
                value,
            )
        }
    }

    @Test
    fun detailsOfOtherTypesAreIgnored() {
        val everything = bank.copy(last4 = "bad", investmentKind = InvestmentKind.Bonds, platform = "X")
        assertEquals(
            setOf(FormField.Direction, FormField.Counterparty),
            checkAccountForm(AccountType.Loan, "Car", "1", everything).invalid,
        )
    }

    @Test
    fun hintForAnEmptyBankForm() {
        assertEquals(
            "Enter a name and amount, choose a bank and kind",
            checkAccountForm(AccountType.BankAccount, "", "", empty).hint,
        )
    }

    @Test
    fun hintForDetailsOnly() {
        assertEquals("Choose a bank and kind", missingFieldsHint(listOf(FormField.Bank, FormField.AccountKind)))
        assertEquals("Enter the last 4 digits", missingFieldsHint(listOf(FormField.Last4)))
    }

    @Test
    fun hintForEachInvestmentAndLoanField() {
        assertEquals("Choose a kind", missingFieldsHint(listOf(FormField.InvestmentKind)))
        assertEquals("Enter a platform", missingFieldsHint(listOf(FormField.Platform)))
        assertEquals("Choose a direction", missingFieldsHint(listOf(FormField.Direction)))
        assertEquals("Enter a counterparty", missingFieldsHint(listOf(FormField.Counterparty)))
        assertEquals(
            "Enter a name, choose a direction, enter a counterparty",
            checkAccountForm(AccountType.Loan, "", "5", empty).hint,
        )
        assertEquals("Enter an amount", missingFieldsHint(listOf(FormField.Amount)))
    }

    @Test
    fun noHintWhenComplete() {
        assertNull(missingFieldsHint(emptyList()))
        assertNull(checkAccountForm(AccountType.Loan, "Car", "1", loan).hint)
    }
}
