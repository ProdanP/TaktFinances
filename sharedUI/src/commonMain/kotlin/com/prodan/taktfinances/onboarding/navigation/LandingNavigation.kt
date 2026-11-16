package com.prodan.taktfinances.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.prodan.taktfinances.onboarding.LandingScreen
import kotlinx.serialization.Serializable

@Serializable
data object LandingRoute : NavKey

fun EntryProviderScope<NavKey>.landingEntry(onNavigateToHome: () -> Unit) {
    entry<LandingRoute> {
        LandingScreen(onNavigateToHome = onNavigateToHome)
    }
}
