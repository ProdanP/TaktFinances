package com.prodan.taktfinances.accounts.domain

fun minorUnits(canonical: String): Long? {
    if (canonical.isEmpty()) return null
    if (canonical.any { it == '-' }) return null
    if (canonical.count { it == '.' } > 1) return null
    if (canonical.any { it != '.' && !it.isDigit() }) return null

    val parts = canonical.split('.')
    val wholePart = parts[0]
    val fractionPart = parts.getOrNull(1).orEmpty()
    if (fractionPart.length > 2) return null
    if (wholePart.isEmpty() && fractionPart.isEmpty()) return null
    if (wholePart.isNotEmpty() && wholePart.any { !it.isDigit() }) return null

    val whole = if (wholePart.isEmpty()) 0L else wholePart.toLongOrNull() ?: return null
    val fraction = when (fractionPart.length) {
        0 -> 0L
        1 -> (fractionPart.toLongOrNull() ?: return null) * 10L
        else -> fractionPart.toLongOrNull() ?: return null
    }
    return whole * 100L + fraction
}
