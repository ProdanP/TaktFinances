package com.prodan.taktfinances

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prodan.taktfinances.home.HomeScreen
import com.prodan.taktfinances.onboarding.LandingScreen
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme

private const val RouteLanding = "landing"
private const val RouteHome = "home"

@Composable
@Preview
fun App(contrast: ThemeContrast = ThemeContrast.Standard) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(colorScheme = financesColorScheme(dark = dark, contrast = contrast)) {
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
                HomeScreen()
            }
        }
    }
}
