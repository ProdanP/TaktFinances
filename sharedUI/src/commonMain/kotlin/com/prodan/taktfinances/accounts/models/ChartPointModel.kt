package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.monthAbbreviation
import com.prodan.taktfinances.accounts.domain.entity.monthLabel
import kotlinx.datetime.YearMonth

internal data class ChartPointModel(val month: YearMonth, val axisLabel: String, val label: String, val balanceMinor: Long, val balance: String)

internal fun List<MonthBalance>.toChartPoints(range: ChartRange): List<ChartPointModel> =
    (range.months?.let { takeLast(it) } ?: this).map {
        ChartPointModel(it.month, monthAbbreviation(it.month), monthLabel(it.month), it.balanceMinor, formatAmount(it.balanceMinor))
    }

/** Spoken summary of the chart, e.g. "Balance, 6 months: June 2026 8,200.00 MDL to November 2026 12,340.00 MDL". */
internal fun List<ChartPointModel>.summary(currency: String): String {
    val first = first()
    val last = last()
    if (size == 1) return "Balance for ${first.label}: ${first.balance} $currency"
    return "Balance, $size months: ${first.label} ${first.balance} $currency to ${last.label} ${last.balance} $currency"
}
