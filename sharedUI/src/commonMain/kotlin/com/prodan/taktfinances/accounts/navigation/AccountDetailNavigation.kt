package com.prodan.taktfinances.accounts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prodan.taktfinances.accounts.AccountDetailScreen
import androidx.savedstate.read
import com.prodan.taktfinances.accounts.domain.AccountRepository

private const val ID = "id"
private const val CHECK_IN = "checkIn"

const val AccountDetailRoute = "account/{$ID}?$CHECK_IN={$CHECK_IN}"

/** Route to Account Detail for [id]; [checkIn] opens the check-in sheet for the current month. */
fun accountDetailRoute(id: Long, checkIn: Boolean = false): String = "account/$id?$CHECK_IN=$checkIn"

fun NavGraphBuilder.accountDetailScreen(repository: AccountRepository, onBack: () -> Unit) {
    composable(
        route = AccountDetailRoute,
        arguments = listOf(
            navArgument(ID) { type = NavType.LongType },
            navArgument(CHECK_IN) {
                type = NavType.BoolType
                defaultValue = false
            },
        ),
    ) { entry ->
        val arguments = entry.arguments
        AccountDetailScreen(
            accountId = arguments?.read { getLong(ID) } ?: 0,
            checkInOnOpen = arguments?.read { getBoolean(CHECK_IN) } ?: false,
            repository = repository,
            onBack = onBack,
        )
    }
}
