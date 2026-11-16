package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.checkInMonths
import com.prodan.taktfinances.accounts.domain.entity.parseAmountMinor
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

sealed interface CheckInResult {
    data object Saved : CheckInResult

    /** Not a Monthly account, a month outside the start month..current month, or no number entered. Nothing was stored. */
    data object Invalid : CheckInResult

    /** The input was valid but the entry could not be stored; no entry changed. */
    data class Failed(val reason: Reason) : CheckInResult
}

/** Saves a Monthly account's balance for [month], replacing an existing entry for that month. */
class CheckInUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: SavedAccount, month: YearMonth, amount: String, today: LocalDate): CheckInResult {
        val period = account.period as? Period.Monthly ?: return CheckInResult.Invalid
        if (month !in checkInMonths(period, today)) return CheckInResult.Invalid
        val amountMinor = parseAmountMinor(amount) ?: return CheckInResult.Invalid
        return when (val outcome = repository.saveMonthlyEntry(period.id, MonthlyEntry(month, amountMinor))) {
            is Outcome.Ok -> CheckInResult.Saved
            is Outcome.Failed -> CheckInResult.Failed(outcome.reason)
        }
    }
}
