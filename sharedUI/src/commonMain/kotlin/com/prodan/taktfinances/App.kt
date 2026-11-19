package com.prodan.taktfinances

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.prodan.taktfinances.accounts.navigation.AccountDetailRoute
import com.prodan.taktfinances.accounts.navigation.AddAccountRoute
import com.prodan.taktfinances.accounts.navigation.accountDetailEntry
import com.prodan.taktfinances.accounts.navigation.addAccountEntry
import com.prodan.taktfinances.home.domain.HomeDestination
import com.prodan.taktfinances.home.domain.homeDestinations
import com.prodan.taktfinances.home.navigation.HomeRoute
import com.prodan.taktfinances.home.navigation.homeEntry
import com.prodan.taktfinances.onboarding.navigation.LandingRoute
import com.prodan.taktfinances.onboarding.navigation.landingEntry
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Screens get their ViewModels from Koin with `koinViewModel()`, scoped to their back stack entry;
 * the shell starts the graph first. Each feature declares its keys and entries in its `navigation`
 * package; this shell owns the back stack and connects one feature to another.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(contrast: ThemeContrast = ThemeContrast.Standard) {
    val dark = isSystemInDarkTheme()
    MaterialExpressiveTheme(colorScheme = financesColorScheme(dark = dark, contrast = contrast)) {
        val backStack = rememberNavBackStack(NavKeys, LandingRoute)
        val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        // My Finances and Account Detail share the window when it fits two panes.
        val twoPane = directive.maxHorizontalPartitions > 1
        val listDetail = rememberListDetailSceneStrategy<NavKey>(directive = directive)
        // Bumped after Account Detail changes an account, so My Finances beside it reads the accounts again.
        var accountsVersion by rememberSaveable { mutableIntStateOf(0) }

        val top = backStack.lastOrNull()
        val selectedTab = (backStack.firstOrNull() as? HomeRoute)?.destination
        // Landing and Add Account fill the window; so does Account Detail when it is the only pane.
        val showNavigation = top is HomeRoute || (top is AccountDetailRoute && twoPane)
        val navigationState = rememberNavigationSuiteScaffoldState()
        LaunchedEffect(showNavigation) {
            if (showNavigation) navigationState.show() else navigationState.hide()
        }

        NavigationSuiteScaffold(
            state = navigationState,
            navigationItems = {
                homeDestinations().forEach { destination ->
                    NavigationSuiteItem(
                        selected = destination == selectedTab,
                        onClick = { backStack.replaceAll(HomeRoute(destination)) },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.title) },
                    )
                }
            },
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sceneStrategies = listOf(listDetail),
                entryProvider = entryProvider {
                    landingEntry(
                        onNavigateToHome = { backStack.replaceAll(HomeRoute()) },
                    )
                    homeEntry(
                        onNavigateToAddAccount = { backStack.add(AddAccountRoute) },
                        onNavigateToAccount = { id, checkIn ->
                            // Opening another account replaces the one in the detail pane.
                            backStack.removeAll { it is AccountDetailRoute }
                            backStack.add(AccountDetailRoute(id, checkIn))
                        },
                        accountsVersion = accountsVersion,
                    )
                    addAccountEntry(
                        onClose = { backStack.removeLastOrNull() },
                    )
                    accountDetailEntry(
                        showBack = !twoPane,
                        onBack = { backStack.removeLastOrNull() },
                        onAccountChanged = { accountsVersion++ },
                        onAccountDeleted = { neighbourId ->
                            accountsVersion++
                            backStack.removeAll { it is AccountDetailRoute }
                            // Beside My Finances the detail pane moves to the neighbour; alone, My Finances shows.
                            if (twoPane && neighbourId != null) backStack.add(AccountDetailRoute(neighbourId))
                        },
                    )
                },
            )
        }
    }
}

/** Leaves [key] as the only entry, as when switching tabs or leaving Landing. */
private fun NavBackStack<NavKey>.replaceAll(key: NavKey) {
    clear()
    add(key)
}

private val HomeDestination.icon: ImageVector
    get() = when (this) {
        HomeDestination.Dashboard -> Icons.Filled.Home
        HomeDestination.MyFinances -> Icons.Filled.AccountBalance
        HomeDestination.Profile -> Icons.Filled.Person
    }

/** Every key the back stack can hold, so it can be saved and restored on Android and desktop. */
private val NavKeys = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LandingRoute::class, LandingRoute.serializer())
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(AddAccountRoute::class, AddAccountRoute.serializer())
            subclass(AccountDetailRoute::class, AccountDetailRoute.serializer())
        }
    }
}
