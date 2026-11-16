package com.prodan.taktfinances.accounts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prodan.taktfinances.accounts.AddAccountScreen

const val AddAccountRoute = "add-account"

fun NavGraphBuilder.addAccountScreen(onClose: () -> Unit) {
    composable(AddAccountRoute) {
        AddAccountScreen(onClose = onClose)
    }
}
