package com.prodan.taktfinances.accounts.domain.usecase

import com.prodan.taktfinances.accounts.domain.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Reason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val created = LocalDate(2026, 11, 2)

class DeleteAccountUseCaseTest {
    private val repository = FakeAccountRepository()
    private val delete = DeleteAccountUseCase(repository)

    private suspend fun add(name: String) = repository.add(
        FinanceAccount(name, Currency.MDL, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"), created),
        StartingPointPayload.Monthly(MonthlyEntry(YearMonth(2026, 11), 100)),
    )

    @Test
    fun deletedReturnsTheNextCard() = runTest {
        add("Salary"); add("Brokerage"); add("Car loan")
        assertEquals(DeleteAccountResult.Deleted(neighbourId = 3), delete(2))
        assertEquals(listOf(2L), repository.deletedIds)
    }

    @Test
    fun onlyAccountLeavesNoNeighbour() = runTest {
        add("Salary")
        assertEquals(DeleteAccountResult.Deleted(neighbourId = null), delete(1))
    }

    @Test
    fun unreadableListAfterDeleteStillCountsAsDeleted() = runTest {
        add("Salary"); add("Brokerage")
        repository.failAll = true
        assertEquals(DeleteAccountResult.Deleted(neighbourId = null), delete(1))
    }

    @Test
    fun failedDeleteRemovesNothing() = runTest {
        add("Salary"); add("Brokerage")
        repository.failDelete = true
        assertEquals(DeleteAccountResult.Failed(Reason.Unavailable), delete(1))
        assertTrue(repository.deletedIds.isEmpty())
        assertEquals(2, repository.accounts.size)
    }
}
