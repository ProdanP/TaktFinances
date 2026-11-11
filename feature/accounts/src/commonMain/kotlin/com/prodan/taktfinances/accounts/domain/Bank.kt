package com.prodan.taktfinances.accounts.domain

/** [colorArgb] and [initials] let each UI draw a monogram badge; real logos can replace it later. */
enum class Bank(val title: String, val initials: String, val colorArgb: Long) {
    Maib("Moldova Agroindbank (MAIB)", "MA", 0xFF0B6B3A),
    Victoriabank("Victoriabank", "VB", 0xFFC8102E),
    Micb("Moldindconbank (MICB)", "MI", 0xFF1F4E9C),
    ProCredit("ProCredit Bank", "PC", 0xFF00873C),
    Mobiasbanca("Mobiasbancă", "MB", 0xFFE8501E),
    Other("Other", "?", 0xFF6B7280),
}

fun banks(): List<Bank> = Bank.entries
