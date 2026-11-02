package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.core.Outcome
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountsViewModel(
    private val accounts: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    private val _effects = Channel<AccountsEffect>(Channel.BUFFERED)
    val effects: Flow<AccountsEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AccountsIntent) {
        when (intent) {
            AccountsIntent.Opened -> refresh()
            AccountsIntent.AddTapped -> viewModelScope.launch {
                _effects.send(AccountsEffect.OpenAddAccount)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, unavailable = false) }
            when (val outcome = accounts.accounts()) {
                is Outcome.Ok -> _state.update {
                    it.copy(loading = false, accounts = outcome.value, unavailable = false)
                }
                is Outcome.Failed -> _state.update {
                    it.copy(loading = false, unavailable = true)
                }
            }
        }
    }
}
