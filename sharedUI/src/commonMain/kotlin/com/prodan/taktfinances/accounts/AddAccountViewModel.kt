package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import com.prodan.taktfinances.accounts.domain.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.AddAccountResult
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.filterAmountInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class AddAccountViewModel(
    private val addAccount: AddAccount,
) : ViewModel() {
    private val _state = MutableStateFlow(AddAccountState())
    val state: StateFlow<AddAccountState> = _state.asStateFlow()

    private val _effects = Channel<AddAccountEffect>(Channel.BUFFERED)
    val effects: Flow<AddAccountEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AddAccountIntent) {
        when (intent) {
            is AddAccountIntent.TypeSelected -> _state.update {
                it.copy(
                    type = intent.type,
                    details = AccountDetailsDraft(),
                    edited = it.edited - DETAIL_FIELDS,
                    left = it.left - DETAIL_FIELDS,
                )
            }
            is AddAccountIntent.CurrencySelected -> _state.update { it.copy(currency = intent.currency) }
            is AddAccountIntent.NameChanged -> typed(FormField.Name) { copy(name = intent.value) }
            is AddAccountIntent.AmountChanged -> typed(FormField.Amount) {
                copy(amount = filterAmountInput(intent.value))
            }
            is AddAccountIntent.BankSelected -> picked(FormField.Bank) { copy(bank = intent.bank) }
            is AddAccountIntent.BankAccountKindSelected -> picked(FormField.AccountKind) {
                copy(bankAccountKind = intent.kind)
            }
            is AddAccountIntent.Last4Changed -> typedDetail(FormField.Last4) {
                copy(last4 = intent.value.filter { c -> c in '0'..'9' }.take(LAST4_LENGTH))
            }
            is AddAccountIntent.InvestmentKindSelected -> picked(FormField.InvestmentKind) {
                copy(investmentKind = intent.kind)
            }
            is AddAccountIntent.PlatformChanged -> typedDetail(FormField.Platform) { copy(platform = intent.value) }
            is AddAccountIntent.LoanDirectionSelected -> picked(FormField.Direction) {
                copy(loanDirection = intent.direction)
            }
            is AddAccountIntent.CounterpartyChanged -> typedDetail(FormField.Counterparty) {
                copy(counterparty = intent.value)
            }
            is AddAccountIntent.FieldFocusChanged -> focusChanged(intent.field, intent.focused)
            AddAccountIntent.SaveTapped -> save()
        }
    }

    /** A text edit: the field's error hides until the user leaves it again. */
    private fun typed(field: FormField, edit: AddAccountState.() -> AddAccountState) {
        _state.update { it.edit().copy(edited = it.edited + field, left = it.left - field) }
    }

    private fun typedDetail(field: FormField, edit: AccountDetailsDraft.() -> AccountDetailsDraft) {
        typed(field) { copy(details = details.edit()) }
    }

    /** A chip or badge tap counts as edited and left at once. */
    private fun picked(field: FormField, edit: AccountDetailsDraft.() -> AccountDetailsDraft) {
        _state.update { it.copy(details = it.details.edit(), left = it.left + field) }
    }

    private fun focusChanged(field: FormField, focused: Boolean) {
        _state.update {
            if (focused) return@update it.copy(focused = field)
            // Focus events can arrive in either order, so leaving never depends on which field is focused now.
            val stillFocused = if (it.focused == field) null else it.focused
            if (field in it.edited) {
                it.copy(focused = stillFocused, edited = it.edited - field, left = it.left + field)
            } else {
                it.copy(focused = stillFocused)
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (!current.canSave) return
        when (val result = addAccount(current.type, current.name, current.currency, current.amount, current.details)) {
            is AddAccountResult.Created -> _effects.trySend(AddAccountEffect.Saved)
            is AddAccountResult.Invalid -> _state.update { it.copy(left = it.left + result.invalid) }
        }
    }

    private companion object {
        const val LAST4_LENGTH = 4
        val DETAIL_FIELDS = setOf(
            FormField.Bank, FormField.AccountKind, FormField.Last4,
            FormField.InvestmentKind, FormField.Platform, FormField.Direction, FormField.Counterparty,
        )
    }
}
