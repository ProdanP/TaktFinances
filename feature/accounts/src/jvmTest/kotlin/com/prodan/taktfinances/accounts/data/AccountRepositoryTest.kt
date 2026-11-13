package com.prodan.taktfinances.accounts.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.prodan.taktfinances.accounts.createAccountRepository
import com.prodan.taktfinances.accounts.data.db.AppDatabase
import com.prodan.taktfinances.accounts.domain.AccountDetails
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.accounts.domain.LoanDirection
import com.prodan.taktfinances.accounts.domain.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private val salary = FinanceAccount("Salary", Currency.MDL, 250000, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"))
private val brokerage = FinanceAccount("Brokerage", Currency.EUR, 150050, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"))
private val car = FinanceAccount("Car", Currency.MDL, 50000, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"))

class AccountRepositoryTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { AppDatabase.Schema.create(it) }
    private val repository = createAccountRepository(driver)

    @AfterTest
    fun close() = driver.close()

    private fun <T : Any> ok(outcome: Outcome<T>): T = assertIs<Outcome.Ok<T>>(outcome).value

    @Test
    fun everyTypeRoundTrips() = runTest {
        for (account in listOf(salary, brokerage, car)) {
            val saved = ok(repository.add(account))
            assertEquals(account, saved.account)
        }
        assertEquals(listOf(salary, brokerage, car), ok(repository.all()).map { it.account })
    }

    @Test
    fun bankWithoutLast4RoundTrips() = runTest {
        val account = salary.copy(details = AccountDetails.BankDetails(Bank.Other, BankAccountKind.Card, null))
        ok(repository.add(account))
        assertEquals(listOf(account), ok(repository.all()).map { it.account })
    }

    @Test
    fun accountsAreOrderedByCreationWithGrowingIds() = runTest {
        val first = ok(repository.add(car))
        val second = ok(repository.add(salary))
        val all = ok(repository.all())
        assertEquals(listOf(first, second), all)
        assertEquals(true, second.id > first.id)
    }

    @Test
    fun emptyStoreGivesEmptyList() = runTest {
        assertEquals(emptyList<SavedAccount>(), ok(repository.all()))
    }

    @Test
    fun unknownCodeIsUnavailable() = runTest {
        ok(repository.add(salary))
        driver.execute(null, "UPDATE finance_account SET bank = 'NOPE'", 0)
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }

    @Test
    fun missingDetailIsUnavailable() = runTest {
        ok(repository.add(car))
        driver.execute(null, "UPDATE finance_account SET counterparty = NULL", 0)
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }

    @Test
    fun closedStoreIsUnavailable() = runTest {
        driver.close()
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.add(salary))
        assertEquals(Outcome.Failed(Reason.Unavailable), repository.all())
    }
}
