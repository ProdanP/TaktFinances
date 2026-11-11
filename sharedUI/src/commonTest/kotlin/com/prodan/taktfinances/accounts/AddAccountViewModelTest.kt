package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.AccountDetails
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddAccountViewModelTest {
    private val repository = createAccountRepository()
    private val viewModel = AddAccountViewModel(AddAccount(repository))
    private val state get() = viewModel.state.value

    private fun send(vararg intents: AddAccountIntent) = intents.forEach(viewModel::onIntent)

    /** Focuses [field], applies [edit], then moves focus away. */
    private fun editAndLeave(field: FormField, edit: AddAccountIntent) = send(
        AddAccountIntent.FieldFocusChanged(field, true),
        edit,
        AddAccountIntent.FieldFocusChanged(field, false),
    )

    private fun fillBank() = send(
        AddAccountIntent.NameChanged("Salary"),
        AddAccountIntent.AmountChanged("2500"),
        AddAccountIntent.BankSelected(Bank.Maib),
        AddAccountIntent.BankAccountKindSelected(BankAccountKind.Savings),
    )

    @Test
    fun startsWithBankAccountAndMdl() {
        assertEquals(AccountType.BankAccount, state.type)
        assertEquals(Currency.MDL, state.currency)
        assertEquals("", state.name)
        assertEquals("", state.amountText)
    }

    @Test
    fun saveIsDisabledOnOpenWithAHint() {
        assertFalse(state.canSave)
        assertEquals("Enter a name and amount, choose a bank and kind", state.hint)
        assertTrue(state.visibleErrors.isEmpty())
    }

    @Test
    fun saveIsEnabledWhenCompleteAndDisabledAfterClearingTheName() {
        fillBank()
        assertTrue(state.canSave)
        assertNull(state.hint)

        send(AddAccountIntent.NameChanged(""))
        assertFalse(state.canSave)
        assertEquals("Enter a name", state.hint)
    }

    @Test
    fun hintShrinksWhileFilling() {
        send(AddAccountIntent.NameChanged("Salary"), AddAccountIntent.AmountChanged("10"))
        assertEquals("Choose a bank and kind", state.hint)
        send(AddAccountIntent.BankSelected(Bank.Victoriabank))
        assertEquals("Choose a kind", state.hint)
    }

    @Test
    fun noErrorBeforeLeavingAField() {
        send(
            AddAccountIntent.FieldFocusChanged(FormField.Name, true),
            AddAccountIntent.NameChanged("A"),
            AddAccountIntent.NameChanged(""),
        )
        assertTrue(state.visibleErrors.isEmpty())
    }

    @Test
    fun nameErrorAfterClearingAndLeavingThenClearsOnEdit() {
        send(AddAccountIntent.FieldFocusChanged(FormField.Name, true), AddAccountIntent.NameChanged("A"))
        send(AddAccountIntent.NameChanged(""))
        // The next field reports focus before the name reports losing it.
        send(
            AddAccountIntent.FieldFocusChanged(FormField.Amount, true),
            AddAccountIntent.FieldFocusChanged(FormField.Name, false),
        )
        assertEquals(setOf(FormField.Name), state.visibleErrors)
        assertEquals(FormField.Amount, state.focused)

        send(AddAccountIntent.NameChanged("M"))
        assertTrue(state.visibleErrors.isEmpty())
    }

    @Test
    fun focusWithoutEditingShowsNoError() {
        send(
            AddAccountIntent.FieldFocusChanged(FormField.Name, true),
            AddAccountIntent.FieldFocusChanged(FormField.Name, false),
        )
        assertTrue(state.visibleErrors.isEmpty())
    }

    @Test
    fun last4ErrorAfterLeaving() {
        editAndLeave(FormField.Last4, AddAccountIntent.Last4Changed("12"))
        assertEquals(setOf(FormField.Last4), state.visibleErrors)
    }

    @Test
    fun amountIsRawWhileFocusedAndFormattedAfter() {
        send(AddAccountIntent.FieldFocusChanged(FormField.Amount, true), AddAccountIntent.AmountChanged("2500,5"))
        assertEquals("2500.5", state.amountText)
        send(AddAccountIntent.FieldFocusChanged(FormField.Amount, false))
        assertEquals("2,500.50", state.amountText)
        send(AddAccountIntent.FieldFocusChanged(FormField.Amount, true))
        assertEquals("2500.5", state.amountText)
    }

    @Test
    fun amountInputIsFiltered() {
        send(AddAccountIntent.AmountChanged("-12a.599"))
        assertEquals("12.59", state.amount)
    }

    @Test
    fun editsKeepOtherFields() {
        send(
            AddAccountIntent.NameChanged("Car"),
            AddAccountIntent.AmountChanged("5"),
            AddAccountIntent.TypeSelected(AccountType.Loan),
            AddAccountIntent.CurrencySelected(Currency.EUR),
        )
        assertEquals(AccountType.Loan, state.type)
        assertEquals(Currency.EUR, state.currency)
        assertEquals("Car", state.name)
        assertEquals("5", state.amount)
    }

    @Test
    fun switchingTypeClearsDetailsAndDisablesSave() {
        fillBank()
        editAndLeave(FormField.Last4, AddAccountIntent.Last4Changed("04"))
        assertEquals(setOf(FormField.Last4), state.visibleErrors)

        send(AddAccountIntent.TypeSelected(AccountType.Loan))
        assertEquals("Salary", state.name)
        assertEquals("2500", state.amount)
        assertNull(state.details.bank)
        assertEquals("", state.details.last4)
        assertTrue(state.visibleErrors.isEmpty())
        assertFalse(state.canSave)
        assertEquals("Choose a direction, enter a counterparty", state.hint)
    }

    @Test
    fun last4KeepsOnlyFourDigits() {
        send(AddAccountIntent.Last4Changed("1234567890123456"))
        assertEquals("1234", state.details.last4)
        send(AddAccountIntent.Last4Changed("12a4"))
        assertEquals("124", state.details.last4)
    }

    @Test
    fun saveWhileIncompleteDoesNothing() {
        send(AddAccountIntent.TypeSelected(AccountType.Loan), AddAccountIntent.SaveTapped)
        assertTrue(repository.all().isEmpty())
    }

    @Test
    fun validInvestmentSaveEmitsSaved() = runTest {
        send(
            AddAccountIntent.TypeSelected(AccountType.Investment),
            AddAccountIntent.CurrencySelected(Currency.EUR),
            AddAccountIntent.InvestmentKindSelected(InvestmentKind.Crypto),
            AddAccountIntent.PlatformChanged("Binance"),
            AddAccountIntent.NameChanged("Brokerage"),
            AddAccountIntent.AmountChanged("1500.50"),
            AddAccountIntent.SaveTapped,
        )
        assertEquals(AddAccountEffect.Saved, viewModel.effects.first())
        assertEquals(150050, repository.all().single().amountMinor)
    }

    @Test
    fun bankAccountSavesWithItsDetails() = runTest {
        fillBank()
        send(AddAccountIntent.Last4Changed("0421"), AddAccountIntent.SaveTapped)
        assertEquals(AddAccountEffect.Saved, viewModel.effects.first())
        assertEquals(
            AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"),
            repository.all().single().details,
        )
    }

    @Test
    fun loanNeedsDirectionAndCounterparty() {
        send(
            AddAccountIntent.TypeSelected(AccountType.Loan),
            AddAccountIntent.NameChanged("Car"),
            AddAccountIntent.AmountChanged("-100"),
        )
        assertEquals("100", state.amount)
        send(AddAccountIntent.LoanDirectionSelected(LoanDirection.Lent))
        assertEquals("Enter a counterparty", state.hint)
        send(AddAccountIntent.CounterpartyChanged("Ana"))
        assertTrue(state.canSave)
    }
}
