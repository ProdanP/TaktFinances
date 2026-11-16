package com.prodan.taktfinances

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.prodan.taktfinances.accounts.navigation.AddAccountRoute
import com.prodan.taktfinances.accounts.navigation.accountDetailRoute
import com.prodan.taktfinances.accounts.navigation.accountDetailScreen
import com.prodan.taktfinances.accounts.navigation.addAccountScreen
import com.prodan.taktfinances.home.navigation.HomeRoute
import com.prodan.taktfinances.home.navigation.homeScreen
import com.prodan.taktfinances.onboarding.navigation.LandingRoute
import com.prodan.taktfinances.onboarding.navigation.landingScreen
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme

/**
 * Screens get their ViewModels from Koin with `koinViewModel()`; the shell starts the graph first.
 * Each feature declares its routes in its `navigation` package; this shell connects one feature to another.
 */
@Composable
fun App(contrast: ThemeContrast = ThemeContrast.Standard) {
    val dark = isSystemInDarkTheme()
    MaterialExpressiveTheme(colorScheme = financesColorScheme(dark = dark, contrast = contrast)) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = LandingRoute,
        ) {
            landingScreen(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LandingRoute) { inclusive = true }
                    }
                },
            )
            homeScreen(
                onNavigateToAddAccount = { navController.navigate(AddAccountRoute) },
                onNavigateToAccount = { id, checkIn -> navController.navigate(accountDetailRoute(id, checkIn)) },
            )
            addAccountScreen(
                onClose = { navController.popBackStack() },
            )
            accountDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
