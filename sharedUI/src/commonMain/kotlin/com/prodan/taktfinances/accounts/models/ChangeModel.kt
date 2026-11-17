package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.MonthChange
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.formatChange
import com.prodan.taktfinances.theme.ChangeDirection
import kotlin.math.abs

/** A signed change ready to show: "+1,530.00 (+14.2%)", its direction and a spoken label. */
internal data class ChangeModel(val text: String, val direction: ChangeDirection, val spoken: String)

internal fun MonthChange.toUi(currency: String): ChangeModel {
    val direction = when {
        amountMinor > 0 -> ChangeDirection.Up
        amountMinor < 0 -> ChangeDirection.Down
        else -> ChangeDirection.Flat
    }
    val word = when (direction) {
        ChangeDirection.Up -> "Increase of"
        ChangeDirection.Down -> "Decrease of"
        ChangeDirection.Flat -> "No change,"
    }
    val percent = percentTenths?.let { ", ${abs(it) / 10}.${abs(it) % 10} percent" }.orEmpty()
    return ChangeModel(formatChange(this), direction, "$word ${formatAmount(abs(amountMinor))} $currency$percent")
}
