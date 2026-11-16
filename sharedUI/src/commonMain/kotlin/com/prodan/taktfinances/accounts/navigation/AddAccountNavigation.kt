package com.prodan.taktfinances.accounts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prodan.taktfinances.accounts.AddAccountScreen
import com.prodan.taktfinances.accounts.domain.AccountRepository

const val AddAccountRoute = "add-account"

fun NavGraphBuilder.addAccountScreen(repository: AccountRepository, onClose: () -> Unit) {
    composable(AddAccountRoute) {
        AddAccountScreen(repository = repository, onClose = onClose)
    }
}
