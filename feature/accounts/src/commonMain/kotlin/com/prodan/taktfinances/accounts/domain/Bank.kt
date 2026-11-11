package com.prodan.taktfinances.accounts.domain

/**
 * [colorArgb] and [initials] let each UI draw a monogram badge; real logos can replace it later.
 * [shortName] labels the badge and the account preview summary.
 */
enum class Bank(val title: String, val shortName: String, val initials: String, val colorArgb: Long) {
    Maib("Moldova Agroindbank (MAIB)", "MAIB", "MA", 0xFF0B6B3A),
    Victoriabank("Victoriabank", "Victoriabank", "VB", 0xFFC8102E),
    Micb("Moldindconbank (MICB)", "MICB", "MI", 0xFF1F4E9C),
    ProCredit("ProCredit Bank", "ProCredit", "PC", 0xFF00873C),
    Mobiasbanca("Mobiasbancă", "Mobiasbancă", "MB", 0xFFE8501E),
    Other("Other", "Other", "?", 0xFF6B7280),
}

fun banks(): List<Bank> = Bank.entries
