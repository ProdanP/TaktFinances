package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.FormField
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.accounts.domain.entity.checkAccountForm
import com.prodan.taktfinances.accounts.domain.entity.hasAmount
import com.prodan.taktfinances.accounts.domain.entity.parseAmountMinor
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

sealed interface AddAccountResult {
    data class Created(val account: SavedAccount) : AddAccountResult
    data class Invalid(val invalid: Set<FormField>) : AddAccountResult
    /** The form was valid but the account could not be stored; nothing was created. */
    data class Failed(val reason: Reason) : AddAccountResult
}

/**
 * Creates an account dated [today]. Bank and Loan are tracked monthly from the entered [amount] as
 * the current month's entry; Investment ignores [amount] and starts at 0.00, tracked by trades.
 */
class AddAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(
        type: AccountType,
        name: String,
        currency: Currency,
        amount: String,
        details: AccountDetailsDraft = AccountDetailsDraft(),
        today: LocalDate,
    ): AddAccountResult {
        val check = checkAccountForm(type, name, amount, details)
        if (!check.canSave) return AddAccountResult.Invalid(check.invalid)
        val account = FinanceAccount(
            name = name.trim(),
            currency = currency,
            details = buildDetails(type, details),
            createdOn = today,
        )
        val start = if (type.hasAmount) {
            StartingPointPayload.Monthly(MonthlyEntry(today.yearMonth, parseAmountMinor(amount)!!))
        } else {
            StartingPointPayload.Transactions(openingBalanceMinor = 0)
        }
        return when (val outcome = repository.add(account, start)) {
            is Outcome.Ok -> AddAccountResult.Created(outcome.value)
            is Outcome.Failed -> AddAccountResult.Failed(outcome.reason)
        }
    }
}

/** Builds the details of [type]; call only after [checkAccountForm] reported no invalid fields. */
private fun buildDetails(type: AccountType, draft: AccountDetailsDraft): AccountDetails = when (type) {
    AccountType.BankAccount -> AccountDetails.BankDetails(
        bank = draft.bank!!,
        kind = draft.bankAccountKind!!,
        last4 = draft.last4.trim().ifEmpty { null },
    )
    AccountType.Investment -> AccountDetails.InvestmentDetails(
        kind = draft.investmentKind!!,
        platform = draft.platform.trim(),
    )
    AccountType.Loan -> AccountDetails.LoanDetails(
        direction = draft.loanDirection!!,
        counterparty = draft.counterparty.trim(),
    )
}
