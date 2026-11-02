package com.prodan.taktfinances.onboarding

sealed interface LandingIntent {
    data object ContinueTapped : LandingIntent
}
