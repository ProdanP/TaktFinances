package com.prodan.taktfinances.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prodan.taktfinances.onboarding.LandingScreen

const val LandingRoute = "landing"

fun NavGraphBuilder.landingScreen(onNavigateToHome: () -> Unit) {
    composable(LandingRoute) {
        LandingScreen(onNavigateToHome = onNavigateToHome)
    }
}
