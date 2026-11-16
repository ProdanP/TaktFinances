package com.prodan.taktfinances.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.prodan.taktfinances.accounts.accountsModule
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.database.AppDatabase
import com.prodan.taktfinances.database.DATABASE_NAME
import com.prodan.taktfinances.database.LEGACY_DATABASE_NAME
import com.prodan.taktfinances.database.buildApp
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.test.verify.verify

private val car = FinanceAccount("Car", Currency.MDL, AccountDetails.LoanDetails(LoanDirection.Borrowed, "Ion"), LocalDate(2026, 10, 2))
private val carStart = StartingPointPayload.Monthly(MonthlyEntry(YearMonth(2026, 10), 50000))

class TaktGraphTest {
    private val dir: File = Files.createTempDirectory("takt").toFile()

    @AfterTest
    fun tearDown() {
        KoinPlatform.getKoinOrNull()?.getOrNull<AppDatabase>()?.close()
        stopKoin()
        dir.deleteRecursively()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun modulesResolve() {
        module { includes(databaseModule(inMemoryDatabase()), accountsModule) }.verify()
    }

    @Test
    fun savedAccountIsReadBack() = runTest {
        startTakt { inMemoryDatabase() }
        val repository = AppDependencies().accountRepository
        assertIs<Outcome.Ok<*>>(repository.add(car, carStart))
        val all = assertIs<Outcome.Ok<List<*>>>(repository.all()).value
        assertEquals(1, all.size)
    }

    @Test
    fun checkInIsReadBackThroughTheGraph() = runTest {
        startTakt { inMemoryDatabase() }
        val repository = AppDependencies().accountRepository
        val saved = assertIs<Outcome.Ok<SavedAccount>>(repository.add(car, carStart)).value
        assertIs<Outcome.Ok<*>>(repository.saveMonthlyEntry(saved.period.id, MonthlyEntry(YearMonth(2026, 11), 45000)))
        val read = assertIs<Outcome.Ok<SavedAccount>>(repository.get(saved.id)).value
        val entries = assertIs<Period.Monthly>(read.period).entries
        assertEquals(listOf(MonthlyEntry(YearMonth(2026, 10), 50000), MonthlyEntry(YearMonth(2026, 11), 45000)), entries)
    }

    @Test
    fun aVersionOneDatabaseIsReplacedByAnEmptyOne() = runTest {
        BundledSQLiteDriver().open(File(dir, DATABASE_NAME).absolutePath).apply {
            execSQL("CREATE TABLE finance_account (id INTEGER PRIMARY KEY, name TEXT, amount_minor INTEGER)")
            execSQL("INSERT INTO finance_account (name, amount_minor) VALUES ('Old', 100)")
            execSQL("PRAGMA user_version = 1")
            close()
        }
        startTakt { jvmAppDatabase(dir) }
        val all = assertIs<Outcome.Ok<List<*>>>(AppDependencies().accountRepository.all()).value
        assertEquals(0, all.size)
        assertIs<Outcome.Ok<*>>(AppDependencies().accountRepository.add(car, carStart))
    }

    @Test
    fun startingTwiceKeepsTheFirstGraphAndDoesNotOpenAnotherDatabase() {
        startTakt { inMemoryDatabase() }
        val first = AppDependencies().accountRepository
        startTakt { error("a running graph must not open another database") }
        assertEquals(first, AppDependencies().accountRepository)
    }

    @Test
    fun accountsSurviveReopeningTheFile() = runTest {
        startTakt { jvmAppDatabase(dir) }
        AppDependencies().accountRepository.add(car, carStart)
        tearDownGraph()
        startTakt { jvmAppDatabase(dir) }
        val all = assertIs<Outcome.Ok<List<*>>>(AppDependencies().accountRepository.all()).value
        assertEquals(1, all.size)
    }

    @Test
    fun legacyDatabaseIsDeletedAndTheNewOneStartsEmpty() = runTest {
        val legacy = listOf("", "-journal", "-wal", "-shm").map { File(dir, LEGACY_DATABASE_NAME + it) }
        legacy.forEach { it.writeText("data from a pre-Room build") }
        startTakt { jvmAppDatabase(dir) }
        val all = assertIs<Outcome.Ok<List<*>>>(AppDependencies().accountRepository.all()).value
        assertEquals(0, all.size)
        legacy.forEach { assertFalse(it.exists(), "${it.name} should be deleted") }
        assertTrue(File(dir, DATABASE_NAME).exists())
    }

    private fun tearDownGraph() {
        KoinPlatform.getKoin().get<AppDatabase>().close()
        stopKoin()
    }

    private fun inMemoryDatabase(): AppDatabase = Room.inMemoryDatabaseBuilder<AppDatabase>().buildApp()
}
