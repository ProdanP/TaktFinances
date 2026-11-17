package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.models.AccountDetailContent
import com.prodan.taktfinances.accounts.models.ChartPointModel
import com.prodan.taktfinances.accounts.models.ChartRange
import com.prodan.taktfinances.accounts.models.CheckInSheetModel
import com.prodan.taktfinances.accounts.models.toChartPoints

internal data class AccountDetailState(
    val content: AccountDetailContent = AccountDetailContent.Loading,
    val range: ChartRange = ChartRange.SixMonths,
    /** The open check-in sheet, or null. */
    val sheet: CheckInSheetModel? = null,
) {
    val chart: List<ChartPointModel>
        get() = (content as? AccountDetailContent.Loaded)?.balances?.toChartPoints(range).orEmpty()
}
