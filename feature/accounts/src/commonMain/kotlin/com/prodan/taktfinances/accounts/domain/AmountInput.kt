package com.prodan.taktfinances.accounts.domain

/**
 * Reduces typed or pasted text to what the amount input accepts: digits and one decimal
 * separator ("." or "," both become "."), at most [AMOUNT_DECIMALS] digits after it.
 * A leading separator becomes "0.".
 */
fun filterAmountInput(raw: String): String = buildString {
    var separatorSeen = false
    var decimals = 0
    for (char in raw) {
        when {
            char.isAsciiDigit() && !separatorSeen -> append(char)
            char.isAsciiDigit() && decimals < AMOUNT_DECIMALS -> {
                append(char)
                decimals++
            }
            (char == '.' || char == ',') && !separatorSeen -> {
                if (isEmpty()) append('0')
                append('.')
                separatorSeen = true
            }
        }
    }
}

/**
 * Parses an amount into minor units (cents). Accepts "." or "," as the separator and a
 * trailing separator ("12." is 12.00). Returns null for blank, malformed, over-precise,
 * negative or overflowing input.
 */
internal fun parseAmountMinor(text: String): Long? {
    val value = text.trim().replace(',', '.')
    if (!AMOUNT.matches(value)) return null
    val whole = value.substringBefore('.')
    val fraction = value.substringAfter('.', "").padEnd(AMOUNT_DECIMALS, '0')
    return (whole + fraction).toLongOrNull()
}

/** Formats minor units with "," thousands separators and exactly 2 decimals: 250050 -> "2,500.50". */
fun formatAmount(minor: Long): String {
    val units = minor / 100
    val cents = (minor % 100).toString().padStart(AMOUNT_DECIMALS, '0')
    val grouped = units.toString().reversed().chunked(3).joinToString(",").reversed()
    return "$grouped.$cents"
}

/**
 * Formats raw amount input for display while it is not being edited. Returns null for empty
 * input, and the input unchanged when it cannot be parsed (e.g. too large).
 */
fun formatAmountInput(text: String): String? {
    if (text.isBlank()) return null
    return parseAmountMinor(text)?.let(::formatAmount) ?: text
}

private val AMOUNT = Regex("""\d+(\.\d{0,$AMOUNT_DECIMALS})?""")

private fun Char.isAsciiDigit() = this in '0'..'9'
