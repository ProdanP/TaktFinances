package com.prodan.taktfinances.accounts.domain.entity

import kotlin.math.abs
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

/** "+1,500.00", "−500.00" (U+2212 minus) or a neutral "0.00". */
fun formatSignedAmount(minor: Long): String = when {
    minor > 0 -> "+" + formatAmount(minor)
    minor < 0 -> MINUS + formatAmount(-minor)
    else -> formatAmount(0)
}

/** Tenths of a percent as "+15.0%", "−5.0%" or "0.0%". */
fun formatPercent(tenths: Long): String {
    val sign = when {
        tenths > 0 -> "+"
        tenths < 0 -> MINUS
        else -> ""
    }
    val value = abs(tenths)
    return "$sign${value / 10}.${value % 10}%"
}

/** "+1,500.00 (+15.0%)", or just the amount when the percentage is hidden. */
fun formatChange(change: MonthChange): String {
    val amount = formatSignedAmount(change.amountMinor)
    val percent = change.percentTenths ?: return amount
    return "$amount (${formatPercent(percent)})"
}

/** "November 2026". */
fun monthLabel(month: YearMonth): String = "${shortMonthLabel(month)} ${month.year}"

/** "November". */
fun shortMonthLabel(month: YearMonth): String = MONTH_NAMES[month.month.number - 1]

/** "Nov", for chart axes. */
fun monthAbbreviation(month: YearMonth): String = shortMonthLabel(month).take(3)

/** True when [text] holds an amount the amount input accepts (see [filterAmountInput]). */
fun isAmount(text: String): Boolean = parseAmountMinor(text) != null

private const val MINUS = "−"

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
