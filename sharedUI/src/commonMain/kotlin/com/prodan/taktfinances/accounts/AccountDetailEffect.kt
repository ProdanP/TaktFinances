package com.prodan.taktfinances.accounts

internal sealed interface AccountDetailEffect {
    /** The account or one of its entries changed; My Finances should read the accounts again. */
    data object AccountChanged : AccountDetailEffect

    /** The account is gone; [neighbourId] is the card to show next, null when none is left. */
    data class AccountDeleted(val neighbourId: Long?) : AccountDetailEffect

    /** Removing the account failed; it is unchanged. */
    data object DeleteFailed : AccountDetailEffect
}

internal const val DELETE_ACCOUNT_FAILED_MESSAGE = "Couldn't delete the account. Try again."
