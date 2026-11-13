package com.prodan.taktfinances

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prodan.taktfinances.accounts.AddAccountScreen
import com.prodan.taktfinances.di.AppDependencies
import com.prodan.taktfinances.home.HomeScreen
import com.prodan.taktfinances.onboarding.LandingScreen
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme

private const val RouteLanding = "landing"
private const val RouteHome = "home"
private const val RouteAddAccount = "add-account"

/** [dependencies] come from the composition root in :sharedLogic; screens get them through constructors. */
@Composable
fun App(dependencies: AppDependencies, contrast: ThemeContrast = ThemeContrast.Standard) {
    val dark = isSystemInDarkTheme()
    MaterialExpressiveTheme(colorScheme = financesColorScheme(dark = dark, contrast = contrast)) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = RouteLanding,
        ) {
            composable(RouteLanding) {
                LandingScreen(
                    onNavigateToHome = {
                        navController.navigate(RouteHome) {
                            popUpTo(RouteLanding) { inclusive = true }
                        }
                    },
                )
            }
            composable(RouteHome) {
                HomeScreen(
                    accounts = dependencies.accountRepository,
                    onNavigateToAddAccount = { navController.navigate(RouteAddAccount) },
                )
            }
            composable(RouteAddAccount) {
                AddAccountScreen(
                    repository = dependencies.accountRepository,
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}
