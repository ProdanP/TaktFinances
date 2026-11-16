package com.prodan.taktfinances.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.components.icon
import com.prodan.taktfinances.home.HomeIntent
import com.prodan.taktfinances.home.HomeState
import com.prodan.taktfinances.home.domain.HomeDestination

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun HomeContent(
    state: HomeState,
    onDestinationSelected: (HomeDestination) -> Unit,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit = {},
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val widthSizeClass = WindowSizeClass.calculateFromSize(
            DpSize(maxWidth, maxHeight),
        ).widthSizeClass

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactHome(
                state = state,
                onDestinationSelected = onDestinationSelected,
                onAddAccount = onAddAccount,
                onIntent = onIntent,
            )
            WindowWidthSizeClass.Medium -> MediumHome(
                state = state,
                onDestinationSelected = onDestinationSelected,
                onAddAccount = onAddAccount,
                onIntent = onIntent,
            )
            else -> ExpandedHome(
                state = state,
                onDestinationSelected = onDestinationSelected,
                onAddAccount = onAddAccount,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun CompactHome(
    state: HomeState,
    onDestinationSelected: (HomeDestination) -> Unit,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DestinationBody(
            state = state,
            onAddAccount = onAddAccount,
            onIntent = onIntent,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        NavigationBar {
            state.destinations.forEach { destination ->
                NavigationBarItem(
                    selected = destination == state.selected,
                    onClick = { onDestinationSelected(destination) },
                    icon = { DestinationIcon(destination) },
                    label = { Text(destination.title) },
                )
            }
        }
    }
}

@Composable
private fun MediumHome(
    state: HomeState,
    onDestinationSelected: (HomeDestination) -> Unit,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            state.destinations.forEach { destination ->
                NavigationRailItem(
                    selected = destination == state.selected,
                    onClick = { onDestinationSelected(destination) },
                    icon = { DestinationIcon(destination) },
                    label = { Text(destination.title) },
                )
            }
        }
        DestinationBody(
            state = state,
            onAddAccount = onAddAccount,
            onIntent = onIntent,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun ExpandedHome(
    state: HomeState,
    onDestinationSelected: (HomeDestination) -> Unit,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet {
                Column(modifier = Modifier.padding(12.dp)) {
                    state.destinations.forEach { destination ->
                        NavigationDrawerItem(
                            selected = destination == state.selected,
                            onClick = { onDestinationSelected(destination) },
                            icon = { DestinationIcon(destination) },
                            label = { Text(destination.title) },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }
        },
    ) {
        DestinationBody(
            state = state,
            onAddAccount = onAddAccount,
            onIntent = onIntent,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationBody(
    state: HomeState,
    onAddAccount: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (state.selected == HomeDestination.MyFinances) {
            TopAppBar(
                title = { Text(state.selected.title) },
                actions = {
                    IconButton(onClick = onAddAccount) {
                        Icon(Icons.Filled.Add, contentDescription = "Add account")
                    }
                },
            )
        }
        if (state.selected == HomeDestination.MyFinances) {
            MyFinancesBody(
                content = state.finances,
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            DestinationMock(
                title = state.selected.title,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DestinationIcon(destination: HomeDestination) {
    Icon(
        imageVector = destination.icon,
        contentDescription = destination.title,
    )
}

private val HomeDestination.icon: ImageVector
    get() = when (this) {
        HomeDestination.Dashboard -> Icons.Filled.Home
        HomeDestination.MyFinances -> Icons.Filled.AccountBalance
        HomeDestination.Profile -> Icons.Filled.Person
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
