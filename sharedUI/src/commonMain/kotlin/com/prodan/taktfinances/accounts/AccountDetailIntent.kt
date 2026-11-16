package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.models.ChartRange
import kotlinx.datetime.YearMonth

sealed interface AccountDetailIntent {
    /** The screen became visible; loads the account (again). */
    data object Shown : AccountDetailIntent
    data object RetryTapped : AccountDetailIntent
    data class RangeSelected(val range: ChartRange) : AccountDetailIntent

    /** The "Check in" action: the sheet for the current month. */
    data object CheckInTapped : AccountDetailIntent

    /** A timeline row or its "Check in" shortcut: the sheet for [month]. */
    data class MonthTapped(val month: YearMonth) : AccountDetailIntent
    data class SheetMonthSelected(val month: YearMonth) : AccountDetailIntent
    data class AmountChanged(val value: String) : AccountDetailIntent
    data class AmountFocusChanged(val focused: Boolean) : AccountDetailIntent
    data object SameAsLastMonthTapped : AccountDetailIntent
    data object SaveTapped : AccountDetailIntent
    data object DeleteTapped : AccountDetailIntent
    data object DeleteConfirmed : AccountDetailIntent
    data object DeleteCancelled : AccountDetailIntent
    data object SheetDismissed : AccountDetailIntent
}
