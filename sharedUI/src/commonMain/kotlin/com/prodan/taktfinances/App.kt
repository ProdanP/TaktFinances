package com.prodan.taktfinances

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prodan.taktfinances.accounts.AddAccountScreen
import com.prodan.taktfinances.home.HomeScreen
import com.prodan.taktfinances.onboarding.LandingScreen
import com.prodan.taktfinances.theme.AppTheme

private const val RouteLanding = "landing"
private const val RouteHome = "home"
private const val RouteAddAccount = "add-account"

@Composable
@Preview
fun App() {
    AppTheme {
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
                    onNavigateToAddAccount = {
                        navController.navigate(RouteAddAccount)
                    },
                )
            }
            composable(RouteAddAccount) {
                AddAccountScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
