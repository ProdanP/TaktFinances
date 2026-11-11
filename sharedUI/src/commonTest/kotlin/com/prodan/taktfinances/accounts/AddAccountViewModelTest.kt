package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.AccountDetails
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection
import com.prodan.taktfinances.accounts.createAccountRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddAccountViewModelTest {
    private val repository = createAccountRepository()
    private val viewModel = AddAccountViewModel(AddAccount(repository))

    @Test
    fun startsWithBankAccountAndMdl() {
        assertEquals(AccountType.BankAccount, viewModel.state.value.type)
        assertEquals(Currency.MDL, viewModel.state.value.currency)
        assertEquals("", viewModel.state.value.name)
    }

    @Test
    fun editsKeepOtherFields() {
        viewModel.onIntent(AddAccountIntent.NameChanged("Car"))
        viewModel.onIntent(AddAccountIntent.AmountChanged("5"))
        viewModel.onIntent(AddAccountIntent.TypeSelected(AccountType.Loan))
        viewModel.onIntent(AddAccountIntent.CurrencySelected(Currency.EUR))
        val state = viewModel.state.value
        assertEquals(AccountType.Loan, state.type)
        assertEquals(Currency.EUR, state.currency)
        assertEquals("Car", state.name)
        assertEquals("5", state.amount)
    }

    @Test
    fun invalidSaveShowsErrorsAndClearsOnEdit() {
        viewModel.onIntent(AddAccountIntent.AmountChanged("abc"))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertTrue(viewModel.state.value.nameInvalid)
        assertTrue(viewModel.state.value.amountInvalid)
        assertTrue(repository.all().isEmpty())

        viewModel.onIntent(AddAccountIntent.NameChanged("Main"))
        assertFalse(viewModel.state.value.nameInvalid)
        assertTrue(viewModel.state.value.amountInvalid)
        viewModel.onIntent(AddAccountIntent.AmountChanged("1"))
        assertFalse(viewModel.state.value.amountInvalid)
    }

    @Test
    fun validSaveEmitsSavedAndStoresAccount() = runTest {
        viewModel.onIntent(AddAccountIntent.TypeSelected(AccountType.Investment))
        viewModel.onIntent(AddAccountIntent.CurrencySelected(Currency.EUR))
        viewModel.onIntent(AddAccountIntent.InvestmentKindSelected(InvestmentKind.Crypto))
        viewModel.onIntent(AddAccountIntent.PlatformChanged("Binance"))
        viewModel.onIntent(AddAccountIntent.NameChanged("Brokerage"))
        viewModel.onIntent(AddAccountIntent.AmountChanged("1500.50"))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertEquals(AddAccountEffect.Saved, viewModel.effects.first())
        assertEquals(150050, repository.all().single().amountMinor)
    }

    @Test
    fun switchingTypeKeepsCommonFieldsAndClearsDetails() {
        viewModel.onIntent(AddAccountIntent.NameChanged("Main"))
        viewModel.onIntent(AddAccountIntent.AmountChanged("5"))
        viewModel.onIntent(AddAccountIntent.BankSelected(Bank.Victoriabank))
        viewModel.onIntent(AddAccountIntent.Last4Changed("0421"))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertTrue(viewModel.state.value.detailErrors.accountKind)

        viewModel.onIntent(AddAccountIntent.TypeSelected(AccountType.Loan))
        val state = viewModel.state.value
        assertEquals("Main", state.name)
        assertEquals("5", state.amount)
        assertEquals(null, state.details.bank)
        assertEquals("", state.details.last4)
        assertFalse(state.detailErrors.any)
    }

    @Test
    fun last4KeepsOnlyFourDigits() {
        viewModel.onIntent(AddAccountIntent.Last4Changed("1234567890123456"))
        assertEquals("1234", viewModel.state.value.details.last4)
        viewModel.onIntent(AddAccountIntent.Last4Changed("12a4"))
        assertEquals("124", viewModel.state.value.details.last4)
    }

    @Test
    fun detailErrorsAreShownTogetherAndClearOnEdit() {
        viewModel.onIntent(AddAccountIntent.TypeSelected(AccountType.Loan))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        val state = viewModel.state.value
        assertTrue(state.nameInvalid)
        assertTrue(state.detailErrors.direction)
        assertTrue(state.detailErrors.counterparty)

        viewModel.onIntent(AddAccountIntent.LoanDirectionSelected(LoanDirection.Lent))
        assertFalse(viewModel.state.value.detailErrors.direction)
        assertTrue(viewModel.state.value.detailErrors.counterparty)
        viewModel.onIntent(AddAccountIntent.CounterpartyChanged("Ana"))
        assertFalse(viewModel.state.value.detailErrors.any)
    }

    @Test
    fun missingBankIsAnErrorAndBankAccountSaves() = runTest {
        viewModel.onIntent(AddAccountIntent.NameChanged("Salary"))
        viewModel.onIntent(AddAccountIntent.AmountChanged("2500"))
        viewModel.onIntent(AddAccountIntent.BankAccountKindSelected(BankAccountKind.Savings))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertTrue(viewModel.state.value.detailErrors.bank)
        assertTrue(repository.all().isEmpty())

        viewModel.onIntent(AddAccountIntent.BankSelected(Bank.Maib))
        viewModel.onIntent(AddAccountIntent.Last4Changed("0421"))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertEquals(AddAccountEffect.Saved, viewModel.effects.first())
        assertEquals(
            AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"),
            repository.all().single().details,
        )
    }
}
