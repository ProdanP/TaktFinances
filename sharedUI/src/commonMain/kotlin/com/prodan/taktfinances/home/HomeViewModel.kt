package com.prodan.taktfinances.home

import androidx.lifecycle.ViewModel
import com.prodan.taktfinances.home.domain.HomeDestination
import com.prodan.taktfinances.home.domain.homeDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HomeState(
            destinations = homeDestinations(),
            selected = HomeDestination.Dashboard,
        ),
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.DestinationSelected -> _state.update {
                it.copy(selected = intent.destination)
            }
        }
    }
}
