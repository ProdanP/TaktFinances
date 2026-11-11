package com.prodan.taktfinances.accounts.domain

sealed interface AccountDetails {
    val type: AccountType

    data class BankDetails(
        val bank: Bank,
        val kind: BankAccountKind,
        val last4: String?,
    ) : AccountDetails {
        override val type get() = AccountType.BankAccount
    }

    data class InvestmentDetails(
        val kind: InvestmentKind,
        val platform: String,
    ) : AccountDetails {
        override val type get() = AccountType.Investment
    }

    data class LoanDetails(
        val direction: LoanDirection,
        val counterparty: String,
    ) : AccountDetails {
        override val type get() = AccountType.Loan
    }
}

/** Every optional detail input; [AddAccount] reads only the fields that belong to the account type. */
data class AccountDetailsDraft(
    val bank: Bank? = null,
    val bankAccountKind: BankAccountKind? = null,
    val last4: String = "",
    val investmentKind: InvestmentKind? = null,
    val platform: String = "",
    val loanDirection: LoanDirection? = null,
    val counterparty: String = "",
)

data class DetailErrors(
    val bank: Boolean = false,
    val accountKind: Boolean = false,
    val last4: Boolean = false,
    val investmentKind: Boolean = false,
    val platform: Boolean = false,
    val direction: Boolean = false,
    val counterparty: Boolean = false,
) {
    val any: Boolean
        get() = bank || accountKind || last4 || investmentKind || platform || direction || counterparty
}
