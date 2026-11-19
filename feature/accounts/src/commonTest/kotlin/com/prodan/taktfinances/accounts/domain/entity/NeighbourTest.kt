package com.prodan.taktfinances.accounts.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val created = LocalDate(2026, 9, 20)

private fun account(id: Long) = SavedAccount(
    id,
    FinanceAccount("A$id", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, null), created),
    Period.Monthly(id, created, listOf(MonthlyEntry(YearMonth(2026, 9), 100))),
)

class NeighbourTest {
    // Cards 1, 4, 9 before the delete; the list passed is what remains.
    @Test
    fun middleCardSelectsTheNext() {
        assertEquals(9L, neighbourAfterRemoving(listOf(account(1), account(9)), removedId = 4)?.id)
    }

    @Test
    fun lastCardSelectsThePrevious() {
        assertEquals(4L, neighbourAfterRemoving(listOf(account(1), account(4)), removedId = 9)?.id)
    }

    @Test
    fun firstCardSelectsTheNext() {
        assertEquals(4L, neighbourAfterRemoving(listOf(account(4), account(9)), removedId = 1)?.id)
    }

    @Test
    fun onlyAccountLeavesNothing() {
        assertNull(neighbourAfterRemoving(emptyList(), removedId = 1))
    }
}
