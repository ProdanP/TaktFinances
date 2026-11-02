package com.prodan.taktfinances.onboarding

sealed interface LandingEffect {
    data object OpenHome : LandingEffect
}
