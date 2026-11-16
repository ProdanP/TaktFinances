package com.prodan.taktfinances.accounts.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prodan.taktfinances.accounts.AccountDetailScreen
import kotlinx.serialization.Serializable

/** Account Detail for [id]; [checkIn] opens the check-in sheet for the current month. */
@Serializable
data class AccountDetailRoute(val id: Long, val checkIn: Boolean = false) : NavKey

/** The detail pane of My Finances' list-detail layout; [showBack] is false while the list is beside it. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.accountDetailEntry(showBack: Boolean, onBack: () -> Unit) {
    entry<AccountDetailRoute>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
        AccountDetailScreen(
            accountId = route.id,
            checkInOnOpen = route.checkIn,
            showBack = showBack,
            onBack = onBack,
        )
    }
}
