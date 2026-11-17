package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.MonthBalance
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.accountSummary
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.monthChanges

internal data class AccountDetailHeaderModel(
    val name: String,
    val type: AccountType,
    val bank: Bank?,
    val summary: String,
    val last4: String?,
    val balance: String,
    val currency: String,
    /** Null when the account has no previous month ("This month —"). */
    val thisMonth: ChangeModel?,
)

internal fun SavedAccount.toHeader(balances: List<MonthBalance>): AccountDetailHeaderModel {
    val currency = account.currency.code
    val bank = account.details as? AccountDetails.BankDetails
    return AccountDetailHeaderModel(
        name = account.name,
        type = account.type,
        bank = bank?.bank,
        summary = accountSummary(account.details),
        last4 = bank?.last4,
        balance = formatAmount(balances.last().balanceMinor),
        currency = currency,
        thisMonth = monthChanges(balances).last()?.toUi(currency),
    )
}
