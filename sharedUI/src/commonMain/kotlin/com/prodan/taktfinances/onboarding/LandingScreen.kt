package com.prodan.taktfinances.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.onboarding.components.LandingContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LandingViewModel = koinViewModel(),
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
