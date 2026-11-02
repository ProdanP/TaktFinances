package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccountDraftProblemsTest {
    @Test
    fun blankNameIsAProblem() {
        val problems = accountDraftProblems(
            name = "   ",
            type = AccountType.Bank,
            currency = Currency.Eur,
            minorUnits = 0L,
        )
        assertEquals(listOf(AccountDraftProblem.BlankName), problems)
    }

    @Test
    fun missingFieldsAreProblems() {
        val problems = accountDraftProblems(
            name = "Cash",
            type = null,
            currency = null,
            minorUnits = null,
        )
        assertEquals(
            listOf(
                AccountDraftProblem.MissingType,
                AccountDraftProblem.MissingCurrency,
                AccountDraftProblem.MissingAmount,
            ),
            problems,
        )
    }

    @Test
    fun zeroAmountIsNotAProblem() {
        val problems = accountDraftProblems(
            name = "Cash",
            type = AccountType.Investment,
            currency = Currency.Mdl,
            minorUnits = 0L,
        )
        assertTrue(problems.isEmpty())
    }
}
