package com.prodan.taktfinances.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prodan.taktfinances.home.HomeScreen

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToAccount: (id: Long, checkIn: Boolean) -> Unit,
) {
    composable(HomeRoute) {
        HomeScreen(
            onNavigateToAddAccount = onNavigateToAddAccount,
            onNavigateToAccount = onNavigateToAccount,
        )
    }
}
