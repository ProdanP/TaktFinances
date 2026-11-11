package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.Currency
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
        viewModel.onIntent(AddAccountIntent.NameChanged("Brokerage"))
        viewModel.onIntent(AddAccountIntent.AmountChanged("1500.50"))
        viewModel.onIntent(AddAccountIntent.SaveTapped)
        assertEquals(AddAccountEffect.Saved, viewModel.effects.first())
        assertEquals(150050, repository.all().single().amountMinor)
    }
}
