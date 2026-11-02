package com.prodan.taktfinances.home.domain

enum class HomeDestination(val title: String) {
    Dashboard("Dashboard"),
    MyFinances("My Finances"),
    Profile("Profile"),
}

fun homeDestinations(): List<HomeDestination> = HomeDestination.entries
