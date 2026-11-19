package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.balanceMinor
import com.prodan.taktfinances.accounts.domain.entity.checkInMonths
import com.prodan.taktfinances.accounts.domain.entity.parseAmountMinor
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.datetime.LocalDate

sealed interface EditAccountResult {
    data object Saved : EditAccountResult

    /** Neither the name nor the balance changed; nothing was stored. */
    data object Unchanged : EditAccountResult

    /** A blank name, or no amount on a Monthly account. Nothing was stored. */
    data object Invalid : EditAccountResult

    /** The input was valid but could not be stored; the account is unchanged. */
    data class Failed(val reason: Reason) : EditAccountResult
}

/**
 * Renames [account] and, for a Monthly account whose [amount] differs from its current balance, stores
 * it as the current month's entry. An unchanged amount writes no entry, so a carried-forward month stays
 * "Not checked in". Investment accounts ignore [amount].
 */
class EditAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: SavedAccount, name: String, amount: String, today: LocalDate): EditAccountResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return EditAccountResult.Invalid
        val period = account.period
        val entry = if (period is Period.Monthly) {
            val amountMinor = parseAmountMinor(amount) ?: return EditAccountResult.Invalid
            // The current month, or the start month when the clock reads an earlier date (D17).
            MonthlyEntry(checkInMonths(period, today).first(), amountMinor)
                .takeIf { amountMinor != account.balanceMinor(today) }
        } else {
            null
        }
        if (trimmed == account.account.name && entry == null) return EditAccountResult.Unchanged
        return when (val outcome = repository.edit(account.id, trimmed, period.id, entry)) {
            is Outcome.Ok -> EditAccountResult.Saved
            is Outcome.Failed -> EditAccountResult.Failed(outcome.reason)
        }
    }
}
