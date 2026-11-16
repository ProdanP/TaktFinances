package com.prodan.taktfinances.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.home.components.MyFinancesBody
import com.prodan.taktfinances.home.domain.HomeDestination
import org.koin.compose.viewmodel.koinViewModel

/** One Home tab; the app shell provides the navigation bar, rail or drawer around it. */
@Composable
fun HomeScreen(
    destination: HomeDestination,
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
    DestinationBody(
        destination = destination,
        state = state,
        onAddAccount = { viewModel.onIntent(HomeIntent.AddAccountTapped) },
        onIntent = viewModel::onIntent,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationBody(
    destination: HomeDestination,
    state: HomeState,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (destination == HomeDestination.MyFinances) {
            TopAppBar(
                title = { Text(destination.title) },
                actions = {
                    IconButton(onClick = onAddAccount) {
                        Icon(Icons.Filled.Add, contentDescription = "Add account")
                    }
                },
            )
        }
        if (destination == HomeDestination.MyFinances) {
            MyFinancesBody(
                content = state.finances,
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            DestinationMock(
                title = destination.title,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DestinationMock(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val contentWidth = maxWidth.coerceAtMost(420.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = contentWidth)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
