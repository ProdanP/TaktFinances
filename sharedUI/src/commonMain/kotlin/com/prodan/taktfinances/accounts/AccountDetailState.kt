package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.models.AccountDetailContent
import com.prodan.taktfinances.accounts.models.ChartPointModel
import com.prodan.taktfinances.accounts.models.ChartRange
import com.prodan.taktfinances.accounts.models.CheckInSheetModel
import com.prodan.taktfinances.accounts.models.EditSheetModel
import com.prodan.taktfinances.accounts.models.toChartPoints

internal data class AccountDetailState(
    val content: AccountDetailContent = AccountDetailContent.Loading,
    val range: ChartRange = ChartRange.SixMonths,
    /** The open check-in sheet, or null. */
    val sheet: CheckInSheetModel? = null,
    /** The open "Edit account" sheet, or null. */
    val edit: EditSheetModel? = null,
    /** The delete-account confirmation is shown. */
    val confirmDeleteAccount: Boolean = false,
    /** A confirmed delete is running; Edit and Delete account do nothing meanwhile. */
    val deleting: Boolean = false,
) {
    val chart: List<ChartPointModel>
        get() = (content as? AccountDetailContent.Loaded)?.balances?.toChartPoints(range).orEmpty()
}
