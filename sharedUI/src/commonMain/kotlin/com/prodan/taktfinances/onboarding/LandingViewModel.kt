package com.prodan.taktfinances.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodan.taktfinances.onboarding.domain.landingWelcome
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LandingViewModel : ViewModel() {
    private val welcome = landingWelcome()
    private val _state = MutableStateFlow(
        LandingState(
            title = welcome.title,
            message = welcome.message,
        ),
    )
    val state: StateFlow<LandingState> = _state.asStateFlow()

    private val _effects = Channel<LandingEffect>(Channel.BUFFERED)
    val effects: Flow<LandingEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: LandingIntent) {
        when (intent) {
            LandingIntent.ContinueTapped -> viewModelScope.launch {
                _effects.send(LandingEffect.OpenHome)
            }
        }
    }
}
