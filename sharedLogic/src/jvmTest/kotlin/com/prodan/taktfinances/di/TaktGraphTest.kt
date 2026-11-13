package com.prodan.taktfinances.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.prodan.taktfinances.accounts.domain.AccountDetails
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import com.prodan.taktfinances.accounts.domain.LoanDirection
import com.prodan.taktfinances.core.Outcome
import kotlinx.coroutines.test.runTest
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.verify.verify
import java.nio.file.Files
import java.util.Properties
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TaktGraphTest {
    @AfterTest
    fun tearDown() = stopKoin()

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun modulesResolve() {
        module { includes(databaseModule(inMemoryDriver()), accountsModule) }.verify()
    }

    @Test
    fun savedAccountIsReadBack() = runTest {
        startTakt(inMemoryDriver())
        val repository = AppDependencies().accountRepository
        val car = FinanceAccount("Car", Currency.MDL, 50000, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"))
        assertIs<Outcome.Ok<*>>(repository.add(car))
        val all = assertIs<Outcome.Ok<List<*>>>(repository.all()).value
        assertEquals(1, all.size)
    }

    @Test
    fun startingTwiceKeepsTheFirstGraph() {
        startTakt(inMemoryDriver())
        val first = AppDependencies().accountRepository
        startTakt(inMemoryDriver())
        assertEquals(first, AppDependencies().accountRepository)
    }

    @Test
    fun accountsSurviveReopeningTheFile() = runTest {
        val dir = Files.createTempDirectory("takt").toFile()
        val car = FinanceAccount("Car", Currency.MDL, 50000, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"))
        jvmDatabaseDriver(dir).use { startTakt(it); AppDependencies().accountRepository.add(car); stopKoin() }
        jvmDatabaseDriver(dir).use {
            startTakt(it)
            val all = assertIs<Outcome.Ok<List<*>>>(AppDependencies().accountRepository.all()).value
            assertEquals(1, all.size)
        }
        dir.deleteRecursively()
    }

    private fun inMemoryDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties(), appSchema)
}
