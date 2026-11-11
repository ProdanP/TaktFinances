package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import com.prodan.taktfinances.accounts.domain.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.AddAccountResult
import com.prodan.taktfinances.accounts.domain.DetailErrors
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
                it.copy(type = intent.type, details = AccountDetailsDraft(), detailErrors = DetailErrors())
            }
            is AddAccountIntent.CurrencySelected -> _state.update {
                it.copy(currency = intent.currency, amountInvalid = false)
            }
            is AddAccountIntent.NameChanged -> _state.update {
                it.copy(name = intent.value, nameInvalid = false)
            }
            is AddAccountIntent.AmountChanged -> _state.update {
                it.copy(amount = intent.value, amountInvalid = false)
            }
            is AddAccountIntent.BankSelected -> editDetails(
                { it.copy(bank = intent.bank) }, { it.copy(bank = false) },
            )
            is AddAccountIntent.BankAccountKindSelected -> editDetails(
                { it.copy(bankAccountKind = intent.kind) }, { it.copy(accountKind = false) },
            )
            is AddAccountIntent.Last4Changed -> editDetails(
                { it.copy(last4 = intent.value.filter(Char::isDigit).take(LAST4_LENGTH)) },
                { it.copy(last4 = false) },
            )
            is AddAccountIntent.InvestmentKindSelected -> editDetails(
                { it.copy(investmentKind = intent.kind) }, { it.copy(investmentKind = false) },
            )
            is AddAccountIntent.PlatformChanged -> editDetails(
                { it.copy(platform = intent.value) }, { it.copy(platform = false) },
            )
            is AddAccountIntent.LoanDirectionSelected -> editDetails(
                { it.copy(loanDirection = intent.direction) }, { it.copy(direction = false) },
            )
            is AddAccountIntent.CounterpartyChanged -> editDetails(
                { it.copy(counterparty = intent.value) }, { it.copy(counterparty = false) },
            )
            AddAccountIntent.SaveTapped -> save()
        }
    }

    private fun editDetails(
        edit: (AccountDetailsDraft) -> AccountDetailsDraft,
        clearError: (DetailErrors) -> DetailErrors,
    ) {
        _state.update { it.copy(details = edit(it.details), detailErrors = clearError(it.detailErrors)) }
    }

    private fun save() {
        val current = _state.value
        when (val result = addAccount(current.type, current.name, current.currency, current.amount, current.details)) {
            is AddAccountResult.Created -> _effects.trySend(AddAccountEffect.Saved)
            is AddAccountResult.Invalid -> _state.update {
                it.copy(
                    nameInvalid = result.nameInvalid,
                    amountInvalid = result.amountInvalid,
                    detailErrors = result.detailErrors,
                )
            }
        }
    }

    private companion object {
        const val LAST4_LENGTH = 4
    }
}
