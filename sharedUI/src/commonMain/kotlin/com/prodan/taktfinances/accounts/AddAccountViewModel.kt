package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.accountDraftProblems
import com.prodan.taktfinances.accounts.domain.minorUnits
import com.prodan.taktfinances.accounts.domain.model.Money
import com.prodan.taktfinances.accounts.domain.model.NewAccount
import com.prodan.taktfinances.core.Outcome
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAccountViewModel(
    private val accounts: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddAccountState())
    val state: StateFlow<AddAccountState> = _state.asStateFlow()

    private val _effects = Channel<AddAccountEffect>(Channel.BUFFERED)
    val effects: Flow<AddAccountEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AddAccountIntent) {
        when (intent) {
            is AddAccountIntent.TypeSelected -> _state.update {
                it.copy(type = intent.type, problems = emptyList())
            }
            is AddAccountIntent.NameChanged -> _state.update {
                it.copy(name = intent.value, problems = emptyList())
            }
            is AddAccountIntent.CurrencySelected -> _state.update {
                it.copy(currency = intent.currency, problems = emptyList())
            }
            is AddAccountIntent.AmountChanged -> _state.update {
                it.copy(
                    amountCanonical = toCanonicalAmountInput(intent.value),
                    problems = emptyList(),
                )
            }
            AddAccountIntent.SaveTapped -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val amount = minorUnits(current.amountCanonical)
        val problems = accountDraftProblems(
            name = current.name,
            type = current.type,
            currency = current.currency,
            minorUnits = amount,
        )
        if (problems.isNotEmpty()) {
            _state.update { it.copy(problems = problems) }
            return
        }
        val type = current.type ?: return
        val currency = current.currency ?: return
        val units = amount ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, unavailable = false) }
            when (
                accounts.add(
                    NewAccount(
                        name = current.name.trim(),
                        type = type,
                        balance = Money(minorUnits = units, currency = currency),
                    ),
                )
            ) {
                is Outcome.Ok -> {
                    _state.update { it.copy(saving = false) }
                    _effects.send(AddAccountEffect.Saved)
                }
                is Outcome.Failed -> _state.update {
                    it.copy(saving = false, unavailable = true)
                }
            }
        }
    }
}
