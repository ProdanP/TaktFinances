package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.AddAccountResult
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
            is AddAccountIntent.TypeSelected -> _state.update { it.copy(type = intent.type) }
            is AddAccountIntent.CurrencySelected -> _state.update {
                it.copy(currency = intent.currency, amountInvalid = false)
            }
            is AddAccountIntent.NameChanged -> _state.update {
                it.copy(name = intent.value, nameInvalid = false)
            }
            is AddAccountIntent.AmountChanged -> _state.update {
                it.copy(amount = intent.value, amountInvalid = false)
            }
            AddAccountIntent.SaveTapped -> save()
        }
    }

    private fun save() {
        val current = _state.value
        when (val result = addAccount(current.type, current.name, current.currency, current.amount)) {
            is AddAccountResult.Created -> _effects.trySend(AddAccountEffect.Saved)
            is AddAccountResult.Invalid -> _state.update {
                it.copy(nameInvalid = result.nameInvalid, amountInvalid = result.amountInvalid)
            }
        }
    }
}
