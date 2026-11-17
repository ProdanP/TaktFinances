package com.prodan.taktfinances.onboarding

internal sealed interface LandingIntent {
    data object ContinueTapped : LandingIntent
}
