package com.prodan.taktfinances.accounts.domain.entity

/** What a new account starts from; [AddAccountUseCase] picks it from the account type. */
sealed interface StartingPointPayload {
    data class Monthly(val entry: MonthlyEntry) : StartingPointPayload
    data class Transactions(val openingBalanceMinor: Long) : StartingPointPayload
}
