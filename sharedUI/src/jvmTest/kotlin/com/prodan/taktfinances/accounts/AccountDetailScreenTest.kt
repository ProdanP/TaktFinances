package com.prodan.taktfinances.accounts

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.prodan.taktfinances.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.usecase.CheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.DeleteCheckInUseCase
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val today = LocalDate(2026, 11, 16)

private val salary = FinanceAccount(
    "Salary", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), LocalDate(2026, 10, 4),
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
class AccountDetailScreenTest {
    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    private val repository = FakeAccountRepository()

    private fun show(test: ComposeUiTest, id: Long) = test.setContent {
        AccountDetailScreen(
            accountId = id,
            checkInOnOpen = false,
            showBack = false,
            onBack = {},
            viewModel = AccountDetailViewModel(id, false, repository, CheckInUseCase(repository), DeleteCheckInUseCase(repository)) { today },
        )
    }

    @Test
    fun failedReadShowsRetryThatLoadsTheAccount() = runComposeUiTest {
        val account = repository.insert(salary, listOf(MonthlyEntry(YearMonth(2026, 10), 1_000_000)))
        repository.failGet = true
        show(this, account.id)
        onNodeWithText(DETAIL_LOAD_FAILED_MESSAGE).assertExists()
        repository.failGet = false
        onNodeWithText("Try again").performClick()
        onNodeWithText("Salary").assertExists()
        onNodeWithText("History").assertExists()
    }

    @Test
    fun checkInOpensTheSheetForThisMonth() = runComposeUiTest {
        val account = repository.insert(salary, listOf(MonthlyEntry(YearMonth(2026, 10), 1_000_000)))
        show(this, account.id)
        onNodeWithText("Enter this account's balance at the end of the month.").assertDoesNotExist()
        // The header button; November's timeline row has a "Check in" button too.
        onAllNodesWithText("Check in").onFirst().performClick()
        onNodeWithText("Enter this account's balance at the end of the month.").assertExists()
    }
}
