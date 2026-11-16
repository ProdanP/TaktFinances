package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.canDeleteEntry
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.datetime.YearMonth

sealed interface DeleteCheckInResult {
    data object Deleted : DeleteCheckInResult

    /** The start month, a month with no entry, or not a Monthly account. Nothing was removed. */
    data object Invalid : DeleteCheckInResult

    /** The entry could not be removed and is unchanged. */
    data class Failed(val reason: Reason) : DeleteCheckInResult
}

/** Removes a Monthly account's entry for [month]; that month then carries the earlier balance forward. */
class DeleteCheckInUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: SavedAccount, month: YearMonth): DeleteCheckInResult {
        val period = account.period as? Period.Monthly ?: return DeleteCheckInResult.Invalid
        if (!period.canDeleteEntry(month)) return DeleteCheckInResult.Invalid
        return when (val outcome = repository.deleteMonthlyEntry(period.id, month)) {
            is Outcome.Ok -> DeleteCheckInResult.Deleted
            is Outcome.Failed -> DeleteCheckInResult.Failed(outcome.reason)
        }
    }
}
