package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.formatAmountInput
import com.prodan.taktfinances.accounts.domain.entity.isAmount
import com.prodan.taktfinances.accounts.models.AccountDetailHeaderModel
import com.prodan.taktfinances.accounts.models.ChartPointModel
import com.prodan.taktfinances.accounts.models.ChartRange
import com.prodan.taktfinances.accounts.models.TimelineRowModel
import com.prodan.taktfinances.accounts.models.toChartPoints
import kotlinx.datetime.YearMonth

data class AccountDetailState(
    val content: AccountDetailContent = AccountDetailContent.Loading,
    val range: ChartRange = ChartRange.SixMonths,
    /** The open check-in sheet, or null. */
    val sheet: CheckInSheet? = null,
) {
    val chart: List<ChartPointModel>
        get() = (content as? AccountDetailContent.Loaded)?.balances?.toChartPoints(range).orEmpty()
}

sealed interface AccountDetailContent {
    data object Loading : AccountDetailContent

    /** The account could not be read; the user can retry. */
    data object Failed : AccountDetailContent

    /** [timeline] is null for an account tracked by trades ("No trades yet", no check-in). */
    data class Loaded(
        val account: SavedAccount,
        val header: AccountDetailHeaderModel,
        val balances: List<MonthBalance>,
        val timeline: List<TimelineRowModel>?,
    ) : AccountDetailContent {
        val canCheckIn: Boolean get() = timeline != null
    }
}

data class MonthOption(val month: YearMonth, val label: String)

enum class SheetError(val message: String) {
    SaveFailed("Couldn't save the check-in. Try again."),
    DeleteFailed("Couldn't delete the check-in. Try again."),
}

/** The check-in sheet for [month]. [previousHint] and [previousAmount] are null on the start month. */
data class CheckInSheet(
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
