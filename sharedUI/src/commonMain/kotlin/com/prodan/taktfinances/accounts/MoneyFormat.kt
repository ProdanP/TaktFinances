package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.model.Currency
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency as JavaCurrency

fun formatMoney(minorUnits: Long, currency: Currency): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = JavaCurrency.getInstance(currency.code)
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(BigDecimal.valueOf(minorUnits, 2))
}

fun toCanonicalAmountInput(proposed: String): String {
    val filtered = buildString {
        var seenDecimal = false
        for (char in proposed) {
            when {
                char.isDigit() -> append(char)
                char == '.' && !seenDecimal -> {
                    append('.')
                    seenDecimal = true
                }
            }
        }
    }
    val parts = filtered.split('.')
    if (parts.size == 1) return parts[0]
    return parts[0] + "." + parts[1].take(2)
}
