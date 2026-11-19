package com.prodan.taktfinances.accounts.models

import com.prodan.taktfinances.accounts.domain.entity.formatAmountInput
import com.prodan.taktfinances.accounts.domain.entity.isAmount

/** The "Edit account" sheet. [showsAmount] is false for an account tracked by trades (name only). */
internal data class EditSheetModel(
    val name: String,
    val amount: String,
    val showsAmount: Boolean,
    val amountFocused: Boolean = false,
    val saving: Boolean = false,
    /** Storing the changes failed; the sheet keeps what the user entered. */
    val failed: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && (!showsAmount || isAmount(amount)) && !saving

    /** Raw input while editing, formatted ("11,500.00") otherwise. */
    val amountText: String get() = if (amountFocused) amount else formatAmountInput(amount).orEmpty()
}

internal const val EDIT_SAVE_FAILED_MESSAGE = "Couldn't save the changes. Try again."
