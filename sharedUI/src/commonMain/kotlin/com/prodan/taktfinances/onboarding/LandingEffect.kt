package com.prodan.taktfinances.onboarding

internal sealed interface LandingEffect {
    data object OpenHome : LandingEffect
}
