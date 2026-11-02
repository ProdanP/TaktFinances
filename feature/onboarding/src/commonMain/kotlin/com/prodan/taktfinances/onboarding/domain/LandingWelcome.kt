package com.prodan.taktfinances.onboarding.domain

data class LandingWelcome(
    val title: String,
    val message: String,
)

fun landingWelcome(): LandingWelcome = LandingWelcome(
    title = "Welcome",
    message = "Takt Finances keeps a clear view of your money, so the next step is yours.",
)
