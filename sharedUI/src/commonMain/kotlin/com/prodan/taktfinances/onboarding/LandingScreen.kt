package com.prodan.taktfinances.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.onboarding.components.LandingContent

@Composable
fun LandingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LandingViewModel = viewModel { LandingViewModel() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            LandingEffect.OpenHome -> onNavigateToHome()
        }
    }
    LandingContent(
        state = state,
        onContinue = { viewModel.onIntent(LandingIntent.ContinueTapped) },
    )
}
