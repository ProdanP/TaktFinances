package com.prodan.taktfinances.home

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.prodan.taktfinances.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.home.components.LOAD_FAILED_MESSAGE
import com.prodan.taktfinances.home.domain.HomeDestination
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val today = LocalDate(2026, 11, 16)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
class HomeScreenTest {
    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    private val repository = FakeAccountRepository()
    private val opened = mutableListOf<String>()

    private fun showMyFinances(test: ComposeUiTest) = test.setContent {
        HomeScreen(
            destination = HomeDestination.MyFinances,
            onNavigateToAddAccount = { opened += "add" },
            onNavigateToAccount = { id, checkIn -> opened += "account $id checkIn=$checkIn" },
            viewModel = HomeViewModel(repository) { today },
        )
    }

    @Test
    fun noAccountsOffersTheFirstOneAndOpensAddAccount() = runComposeUiTest {
        showMyFinances(this)
        onNodeWithText("Add your first account").performClick()
        assertEquals(listOf("add"), opened)
    }

    @Test
    fun failedReadShowsRetryThatLoadsTheAccounts() = runComposeUiTest {
        repository.failAll = true
        showMyFinances(this)
        onNodeWithText(LOAD_FAILED_MESSAGE).assertExists()
        repository.failAll = false
        val loan = FinanceAccount("Car loan", Currency.MDL, AccountDetails.LoanDetails(LoanDirection.Lent, "Ana"), LocalDate(2026, 10, 3))
        repository.insert(loan, listOf(MonthlyEntry(YearMonth(2026, 10), 500_000), MonthlyEntry(YearMonth(2026, 11), 450_000)))
        onNodeWithText("Try again").performClick()
        onNodeWithText(LOAD_FAILED_MESSAGE).assertDoesNotExist()
        onNodeWithText("See all").performClick()
        assertEquals(listOf("account 1 checkIn=false"), opened)
    }
}
