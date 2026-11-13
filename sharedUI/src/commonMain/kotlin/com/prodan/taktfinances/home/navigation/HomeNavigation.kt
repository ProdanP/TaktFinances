package com.prodan.taktfinances.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.home.HomeScreen

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(accounts: AccountRepository, onNavigateToAddAccount: () -> Unit) {
    composable(HomeRoute) {
        HomeScreen(accounts = accounts, onNavigateToAddAccount = onNavigateToAddAccount)
    }
}
