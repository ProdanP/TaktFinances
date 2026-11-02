package com.prodan.taktfinances.onboarding

import androidx.lifecycle.ViewModel
import com.prodan.taktfinances.onboarding.domain.landingWelcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LandingViewModel : ViewModel() {
    private val welcome = landingWelcome()
    private val _state = MutableStateFlow(
        LandingState(
            title = welcome.title,
            message = welcome.message,
        ),
    )
    val state: StateFlow<LandingState> = _state.asStateFlow()

    fun onIntent(intent: LandingIntent) {
        when (intent) {
            LandingIntent.ContinueTapped -> Unit
        }
    }
}
