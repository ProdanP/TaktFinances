package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.formatAmountInput
import com.prodan.taktfinances.accounts.domain.entity.isAmount
import kotlinx.datetime.YearMonth

/** The check-in sheet for [month]. [previousHint] and [previousAmount] are null on the start month. */
internal data class CheckInSheetModel(
    val month: YearMonth,
    val monthLabel: String,
    val months: List<MonthOption>,
    val amount: String,
    val amountFocused: Boolean = false,
    /** "October: 10,000.00". */
    val previousHint: String?,
    /** Raw input for "Same as last month". */
    val previousAmount: String?,
    val canDelete: Boolean,
    val saving: Boolean = false,
    val confirmDelete: Boolean = false,
    val error: SheetError? = null,
) {
    val canSave: Boolean get() = isAmount(amount) && !saving

    /** Raw input while editing, formatted ("11,500.00") otherwise. */
    val amountText: String get() = if (amountFocused) amount else formatAmountInput(amount).orEmpty()
}

internal data class MonthOption(val month: YearMonth, val label: String)

internal enum class SheetError(val message: String) {
    SaveFailed("Couldn't save the check-in. Try again."),
    DeleteFailed("Couldn't delete the check-in. Try again."),
}
