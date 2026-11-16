package com.prodan.taktfinances.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.home.domain.HomeDestination
import com.prodan.taktfinances.home.domain.homeDestinations
import com.prodan.taktfinances.home.models.MyFinancesContent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val accounts: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeState(
            destinations = homeDestinations(),
            selected = HomeDestination.Dashboard,
        ),
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    /** Ids from the last successful read; null before the first one. */
    private var knownIds: Set<Long>? = null

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.DestinationSelected -> _state.update { it.copy(selected = intent.destination) }
            HomeIntent.AddAccountTapped, HomeIntent.AddFirstAccountTapped -> _effects.trySend(HomeEffect.OpenAddAccount)
            HomeIntent.Shown, HomeIntent.RetryTapped -> reload()
            is HomeIntent.AccountSelected -> select { accounts -> accounts.firstOrNull { it.id == intent.id } }
            HomeIntent.PreviousTapped -> select { accounts -> accounts.getOrNull(selectedIndex - 1) }
            HomeIntent.NextTapped -> select { accounts -> accounts.getOrNull(selectedIndex + 1) }
        }
    }

    private fun reload() {
        viewModelScope.launch {
            val content = when (val outcome = accounts.all()) {
                is Outcome.Failed -> MyFinancesContent.Failed
                is Outcome.Ok -> contentFor(outcome.value)
            }
            _state.update { it.copy(finances = content) }
        }
    }

    /**
     * A newly added account is selected; otherwise the previous selection is kept while it
     * still exists, and the first card is selected when it does not.
     */
    private fun contentFor(list: List<SavedAccount>): MyFinancesContent {
        val previousIds = knownIds
        knownIds = list.map { it.id }.toSet()
        if (list.isEmpty()) return MyFinancesContent.Empty
        val previousSelection = (_state.value.finances as? MyFinancesContent.Accounts)?.selectedId
        val added = if (previousIds == null) emptyList() else list.filter { it.id !in previousIds }
        val selectedId = when {
            added.isNotEmpty() -> added.maxOf { it.id }
            previousSelection != null && list.any { it.id == previousSelection } -> previousSelection
            else -> list.first().id
        }
        return MyFinancesContent.Accounts(list, selectedId)
    }

    private fun select(pick: MyFinancesContent.Accounts.(List<SavedAccount>) -> SavedAccount?) {
        _state.update { state ->
            val content = state.finances as? MyFinancesContent.Accounts ?: return@update state
            val target = content.pick(content.accounts) ?: return@update state
            state.copy(finances = content.copy(selectedId = target.id))
        }
    }
}
