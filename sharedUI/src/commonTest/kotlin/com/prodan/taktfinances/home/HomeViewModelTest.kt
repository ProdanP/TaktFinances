package com.prodan.taktfinances.home

import com.prodan.taktfinances.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.LoanDirection
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.home.models.ActivityRow
import com.prodan.taktfinances.home.models.RecentActivityModel
import com.prodan.taktfinances.home.models.MyFinancesContent
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val created = LocalDate(2026, 9, 3)
private val today = LocalDate(2026, 11, 16)

private fun loan(name: String) = FinanceAccount(name, Currency.MDL, AccountDetails.LoanDetails(LoanDirection.Lent, "Ana"), created)

private fun entry(month: Int, minor: Long) = MonthlyEntry(YearMonth(2026, month), minor)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    private val repository = FakeAccountRepository()
    private val viewModel = HomeViewModel(repository) { today }
    private val finances get() = viewModel.state.value.finances
    private val accounts get() = assertIs<MyFinancesContent.Accounts>(finances)

    private fun send(vararg intents: HomeIntent) = intents.forEach(viewModel::onIntent)

    @Test
    fun loadingUntilFirstRead() {
        assertEquals(MyFinancesContent.Loading, finances)
    }

    @Test
    fun noAccountsIsEmpty() {
        send(HomeIntent.Shown)
        assertEquals(MyFinancesContent.Empty, finances)
    }

    @Test
    fun failedReadThenRetry() {
        repository.failAll = true
        send(HomeIntent.Shown)
        assertEquals(MyFinancesContent.Failed, finances)
        repository.failAll = false
        repository.insert(loan("Car"))
        send(HomeIntent.RetryTapped)
        assertEquals(listOf("Car"), accounts.accounts.map { it.account.name })
    }

    @Test
    fun firstLoadSelectsFirstCardInCreationOrder() {
        listOf("Salary", "Brokerage", "Car").forEach { repository.insert(loan(it)) }
        send(HomeIntent.Shown)
        assertEquals(listOf("Salary", "Brokerage", "Car"), accounts.accounts.map { it.account.name })
        assertEquals(0, accounts.selectedIndex)
        assertFalse(accounts.canSelectPrevious)
        assertTrue(accounts.canSelectNext)
    }

    @Test
    fun newAccountIsSelectedAfterSave() {
        repository.insert(loan("Salary"))
        repository.insert(loan("Brokerage"))
        send(HomeIntent.Shown)
        val added = repository.insert(loan("Car loan"))
        send(HomeIntent.Shown)
        assertEquals(added.id, accounts.selectedId)
        assertEquals(2, accounts.selectedIndex)
    }

    @Test
    fun firstAccountAfterEmptyIsSelected() {
        send(HomeIntent.Shown)
        val added = repository.insert(loan("First"))
        send(HomeIntent.Shown)
        assertEquals(added.id, accounts.selectedId)
    }

    @Test
    fun returningWithoutSavingKeepsSelection() {
        repository.insert(loan("Salary"))
        val second = repository.insert(loan("Brokerage"))
        send(HomeIntent.Shown, HomeIntent.AccountSelected(second.id), HomeIntent.Shown)
        assertEquals(second.id, accounts.selectedId)
    }

    @Test
    fun removedSelectionFallsBackToFirst() {
        val first = repository.insert(loan("Salary"))
        val second = repository.insert(loan("Brokerage"))
        send(HomeIntent.Shown, HomeIntent.AccountSelected(second.id))
        repository.remove(second.id)
        send(HomeIntent.Shown)
        assertEquals(first.id, accounts.selectedId)
    }

    @Test
    fun previousAndNextStopAtTheEnds() {
        listOf("A", "B", "C").forEach { repository.insert(loan(it)) }
        send(HomeIntent.Shown, HomeIntent.PreviousTapped)
        assertEquals(0, accounts.selectedIndex)
        send(HomeIntent.NextTapped, HomeIntent.NextTapped, HomeIntent.NextTapped)
        assertEquals(2, accounts.selectedIndex)
        assertFalse(accounts.canSelectNext)
        send(HomeIntent.PreviousTapped)
        assertEquals(1, accounts.selectedIndex)
        assertTrue(accounts.canSelectPrevious && accounts.canSelectNext)
    }

    @Test
    fun unknownSelectionIsIgnored() {
        repository.insert(loan("A"))
        send(HomeIntent.Shown)
        val before = accounts.selectedId
        send(HomeIntent.AccountSelected(999))
        assertEquals(before, accounts.selectedId)
    }

    @Test
    fun addFirstAccountOpensAddAccount() = runTest {
        send(HomeIntent.AddFirstAccountTapped)
        assertEquals(HomeEffect.OpenAddAccount, viewModel.effects.first())
    }

    @Test
    fun cardsShowTheCurrentBalance() {
        val car = repository.insert(loan("Car"), listOf(entry(9, 250_000), entry(10, 310_000)))
        send(HomeIntent.Shown)
        assertEquals(310_000, accounts.balances[car.id])
    }

    @Test
    fun recentActivityListsTheNewestEntries() {
        repository.insert(loan("Car"), listOf(entry(9, 900_000), entry(10, 1_000_000), entry(11, 1_150_000)))
        send(HomeIntent.Shown)
        val activity = assertIs<RecentActivityModel.Entries>(accounts.activity)
        assertEquals(
            listOf(ActivityRow("November 2026", "11,500.00"), ActivityRow("October 2026", "10,000.00"), ActivityRow("September 2026", "9,000.00")),
            activity.rows,
        )
        assertEquals("MDL", activity.currency)
    }

    @Test
    fun recentActivityKeepsFiveEntries() {
        val longer = loan("Old").copy(createdOn = LocalDate(2026, 1, 1))
        repository.insert(longer, (1..8).map { entry(it, it * 100L) })
        send(HomeIntent.Shown)
        assertEquals(5, assertIs<RecentActivityModel.Entries>(accounts.activity).rows.size)
    }

    @Test
    fun onlyTheStartMonthMeansNoCheckIns() {
        repository.insert(loan("Car"), listOf(entry(9, 1)))
        send(HomeIntent.Shown)
        assertEquals(RecentActivityModel.NoCheckIns, accounts.activity)
    }

    @Test
    fun investmentWithoutTradesSaysNoTrades() {
        val brokerage = FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), created)
        val saved = repository.insert(brokerage, trades = true)
        send(HomeIntent.Shown)
        assertEquals(RecentActivityModel.NoTrades, accounts.activity)
        assertEquals(0, accounts.balances[saved.id])
    }

    @Test
    fun cardSeeAllAndCheckInOpenAccountDetail() = runTest {
        send(HomeIntent.AccountTapped(4))
        assertEquals(HomeEffect.OpenAccount(4, checkIn = false), viewModel.effects.first())
        send(HomeIntent.SeeAllTapped(5))
        assertEquals(HomeEffect.OpenAccount(5, checkIn = false), viewModel.effects.first())
        send(HomeIntent.CheckInTapped(6))
        assertEquals(HomeEffect.OpenAccount(6, checkIn = true), viewModel.effects.first())
    }
}
