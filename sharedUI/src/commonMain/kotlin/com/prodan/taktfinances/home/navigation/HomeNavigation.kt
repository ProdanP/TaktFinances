package com.prodan.taktfinances.home.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prodan.taktfinances.home.HomeScreen
import com.prodan.taktfinances.home.components.AccountDetailPlaceholder
import com.prodan.taktfinances.home.domain.HomeDestination
import kotlinx.serialization.Serializable

/** A top-level Home tab; the shell's navigation bar, rail or drawer switches between them. */
@Serializable
data class HomeRoute(val destination: HomeDestination = HomeDestination.Dashboard) : NavKey

/** My Finances is the list pane of a list-detail layout, with Account Detail as its detail pane. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.homeEntry(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccount: (id: Long, checkIn: Boolean) -> Unit,
) {
    entry<HomeRoute>(
        metadata = { route ->
            if (route.destination == HomeDestination.MyFinances) {
                ListDetailSceneStrategy.listPane(detailPlaceholder = { AccountDetailPlaceholder() })
            } else {
                emptyMap()
            }
        },
    ) { route ->
        HomeScreen(
            destination = route.destination,
            onNavigateToAddAccount = onNavigateToAddAccount,
            onNavigateToAccount = onNavigateToAccount,
        )
    }
}
