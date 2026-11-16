package com.prodan.taktfinances.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.home.components.HomeContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccount: (id: Long, checkIn: Boolean) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Reload on every start, so returning from Add Account shows (and selects) the new account,
    // and returning from Account Detail shows its new balance.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onIntent(HomeIntent.Shown) }
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            HomeEffect.OpenAddAccount -> onNavigateToAddAccount()
            is HomeEffect.OpenAccount -> onNavigateToAccount(effect.id, effect.checkIn)
        }
    }
    HomeContent(
        state = state,
        onDestinationSelected = { destination ->
            viewModel.onIntent(HomeIntent.DestinationSelected(destination))
        },
        onAddAccount = { viewModel.onIntent(HomeIntent.AddAccountTapped) },
        onIntent = viewModel::onIntent,
    )
}
