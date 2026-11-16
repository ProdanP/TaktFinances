package com.prodan.taktfinances.accounts.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prodan.taktfinances.accounts.AddAccountScreen
import kotlinx.serialization.Serializable

@Serializable
data object AddAccountRoute : NavKey

fun EntryProviderScope<NavKey>.addAccountEntry(onClose: () -> Unit) {
    entry<AddAccountRoute> {
        AddAccountScreen(onClose = onClose)
    }
}
