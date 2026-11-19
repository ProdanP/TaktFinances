package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.neighbourAfterRemoving
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason

sealed interface DeleteAccountResult {
    /** The account is gone; [neighbourId] is the card to select, null when none is left or it couldn't be read. */
    data class Deleted(val neighbourId: Long?) : DeleteAccountResult

    /** Nothing was removed. */
    data class Failed(val reason: Reason) : DeleteAccountResult
}

/** Removes an account with all its entries and picks the neighbouring card to select. */
class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: Long): DeleteAccountResult {
        when (val outcome = repository.delete(id)) {
            is Outcome.Failed -> return DeleteAccountResult.Failed(outcome.reason)
            is Outcome.Ok -> Unit
        }
        val remaining = (repository.all() as? Outcome.Ok)?.value.orEmpty()
        return DeleteAccountResult.Deleted(neighbourAfterRemoving(remaining, id)?.id)
    }
}
