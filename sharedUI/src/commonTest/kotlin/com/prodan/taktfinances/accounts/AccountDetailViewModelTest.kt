package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.FakeAccountRepository
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.usecase.CheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.DeleteAccountUseCase
import com.prodan.taktfinances.accounts.domain.usecase.DeleteCheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.EditAccountUseCase
import com.prodan.taktfinances.accounts.models.AccountDetailContent
import com.prodan.taktfinances.accounts.models.ChartRange
import com.prodan.taktfinances.accounts.models.EDIT_SAVE_FAILED_MESSAGE
import com.prodan.taktfinances.accounts.models.SheetError
import com.prodan.taktfinances.theme.ChangeDirection
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

private val today = LocalDate(2026, 11, 16)
private fun month(m: Int, year: Int = 2026) = YearMonth(year, m)
private fun entry(m: Int, minor: Long, year: Int = 2026) = MonthlyEntry(month(m, year), minor)

private val salary = FinanceAccount(
    "Salary", Currency.MDL, AccountDetails.BankDetails(Bank.Maib, BankAccountKind.Savings, "0421"), LocalDate(2026, 6, 4),
)

/** June start, August not checked in, as in the mockups. */
private val salaryEntries = listOf(
    entry(6, 820_000), entry(7, 895_000), entry(9, 960_000), entry(10, 1_081_000), entry(11, 1_234_000),
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDetailViewModelTest {
    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    private val repository = FakeAccountRepository()

    private fun open(account: SavedAccount, checkIn: Boolean = false): AccountDetailViewModel =
        AccountDetailViewModel(
            account.id,
            checkIn,
            repository,
            CheckInUseCase(repository),
            DeleteCheckInUseCase(repository),
            EditAccountUseCase(repository),
            DeleteAccountUseCase(repository),
        ) { today }.also { it.onIntent(AccountDetailIntent.Shown) }

    private val AccountDetailViewModel.loaded get() = assertIs<AccountDetailContent.Loaded>(state.value.content)
    private val AccountDetailViewModel.sheet get() = assertNotNull(state.value.sheet)
    private val AccountDetailViewModel.edit get() = assertNotNull(state.value.edit)

    /** Every effect [vm] sends from now on, collected for the rest of the test. */
    private fun TestScope.effectsOf(vm: AccountDetailViewModel): List<AccountDetailEffect> =
        mutableListOf<AccountDetailEffect>().also { list ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.collect { list += it } }
        }

    @Test
    fun loadsHeaderTimelineAndChart() {
        val vm = open(repository.insert(salary, salaryEntries))
        val header = vm.loaded.header
        assertEquals("12,340.00", header.balance)
        assertEquals("Savings · MAIB", header.summary)
        assertEquals("+1,530.00 (+14.2%)", header.thisMonth?.text)
        assertEquals(ChangeDirection.Up, header.thisMonth?.direction)
        assertEquals("Increase of 1,530.00 MDL, 14.2 percent", header.thisMonth?.spoken)

        val timeline = assertNotNull(vm.loaded.timeline)
        assertEquals(listOf("November 2026", "October 2026", "September 2026", "August 2026", "July 2026", "June 2026"), timeline.map { it.label })
        val august = timeline[3]
        assertFalse(august.checkedIn)
        assertEquals("8,950.00", august.balance)
        assertNull(timeline.last().change)
        assertEquals("+750.00 (+9.1%)", timeline[4].change?.text)
        assertEquals(6, vm.state.value.chart.size)
    }

    @Test
    fun newAccountHasNoChangeThisMonth() {
        val vm = open(repository.insert(salary.copy(createdOn = today), listOf(entry(11, 250_000))))
        assertNull(vm.loaded.header.thisMonth)
        assertEquals(1, vm.state.value.chart.size)
    }

    @Test
    fun loadFailureAndRetry() {
        val account = repository.insert(salary, salaryEntries)
        repository.failGet = true
        val vm = open(account)
        assertEquals(AccountDetailContent.Failed, vm.state.value.content)
        repository.failGet = false
        vm.onIntent(AccountDetailIntent.RetryTapped)
        assertIs<AccountDetailContent.Loaded>(vm.state.value.content)
    }

    @Test
    fun chartRangesFilterMonths() {
        val long = salary.copy(createdOn = LocalDate(2025, 10, 1))
        val vm = open(repository.insert(long, listOf(entry(10, 1, year = 2025))))
        assertEquals(6, vm.state.value.chart.size)
        assertEquals(month(11), vm.state.value.chart.last().month)
        vm.onIntent(AccountDetailIntent.RangeSelected(ChartRange.OneYear))
        assertEquals(12, vm.state.value.chart.size)
        vm.onIntent(AccountDetailIntent.RangeSelected(ChartRange.All))
        assertEquals(14, vm.state.value.chart.size)
    }

    @Test
    fun checkInOpensTheCurrentMonthWithHintAndShortcut() {
        val vm = open(repository.insert(salary, salaryEntries.dropLast(1)))
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        val sheet = vm.sheet
        assertEquals(month(11), sheet.month)
        assertEquals("", sheet.amount)
        assertFalse(sheet.canSave)
        assertEquals("October: 10,810.00", sheet.previousHint)
        assertEquals((11 downTo 6).map { month(it) }, sheet.months.map { it.month })
        assertFalse(sheet.canDelete)
        vm.onIntent(AccountDetailIntent.SameAsLastMonthTapped)
        assertEquals("10810.00", vm.sheet.amount)
        assertTrue(vm.sheet.canSave)
    }

    @Test
    fun startMonthHasNoHintAndCantBeDeleted() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.MonthTapped(month(6)))
        assertNull(vm.sheet.previousHint)
        assertNull(vm.sheet.previousAmount)
        assertEquals("8200.00", vm.sheet.amount)
        assertFalse(vm.sheet.canDelete)
    }

    @Test
    fun missingMonthOpensEmptyWithCarriedHint() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.MonthTapped(month(8)))
        assertEquals("", vm.sheet.amount)
        assertEquals("July: 8,950.00", vm.sheet.previousHint)
        assertFalse(vm.sheet.canDelete)
    }

    @Test
    fun savingClosesTheSheetAndUpdatesTheScreen() {
        val vm = open(repository.insert(salary, salaryEntries.dropLast(1)))
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        vm.onIntent(AccountDetailIntent.AmountChanged("12340"))
        vm.onIntent(AccountDetailIntent.SaveTapped)
        assertNull(vm.state.value.sheet)
        assertEquals("12,340.00", vm.loaded.header.balance)
        assertEquals("+1,530.00 (+14.2%)", vm.loaded.header.thisMonth?.text)
    }

    @Test
    fun editingAMonthRecalculatesTheNextChange() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.MonthTapped(month(10)))
        assertEquals("10810.00", vm.sheet.amount)
        vm.onIntent(AccountDetailIntent.AmountChanged("11340"))
        vm.onIntent(AccountDetailIntent.SaveTapped)
        assertEquals("+1,000.00 (+8.8%)", vm.loaded.header.thisMonth?.text)
    }

    @Test
    fun failedSaveKeepsTheSheet() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        repository.failSave = true
        vm.onIntent(AccountDetailIntent.AmountChanged("1"))
        vm.onIntent(AccountDetailIntent.SaveTapped)
        assertEquals(SheetError.SaveFailed, vm.sheet.error)
        assertEquals("Couldn't save the check-in. Try again.", vm.sheet.error?.message)
        assertEquals("1", vm.sheet.amount)
        assertFalse(vm.sheet.saving)
    }

    @Test
    fun doubleTapSavesOnce() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        vm.onIntent(AccountDetailIntent.AmountChanged("1"))
        val gate = CompletableDeferred<Unit>()
        repository.saveGate = gate
        vm.onIntent(AccountDetailIntent.SaveTapped)
        vm.onIntent(AccountDetailIntent.SaveTapped)
        assertTrue(vm.sheet.saving)
        gate.complete(Unit)
        assertEquals(1, repository.saveCalls)
    }

    @Test
    fun deleteAsksThenCarriesTheEarlierBalanceForward() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.MonthTapped(month(10)))
        assertTrue(vm.sheet.canDelete)
        vm.onIntent(AccountDetailIntent.DeleteTapped)
        assertTrue(vm.sheet.confirmDelete)
        vm.onIntent(AccountDetailIntent.DeleteCancelled)
        assertFalse(vm.sheet.confirmDelete)
        vm.onIntent(AccountDetailIntent.DeleteTapped)
        vm.onIntent(AccountDetailIntent.DeleteConfirmed)
        assertNull(vm.state.value.sheet)
        val october = vm.loaded.timeline!!.first { it.month == month(10) }
        assertFalse(october.checkedIn)
        assertEquals("9,600.00", october.balance)
    }

    @Test
    fun failedDeleteKeepsTheEntry() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.MonthTapped(month(10)))
        repository.failDelete = true
        vm.onIntent(AccountDetailIntent.DeleteTapped)
        vm.onIntent(AccountDetailIntent.DeleteConfirmed)
        assertEquals(SheetError.DeleteFailed, vm.sheet.error)
        assertTrue(vm.loaded.timeline!!.first { it.month == month(10) }.checkedIn)
    }

    @Test
    fun checkInFlagOpensTheSheetOnce() {
        val vm = open(repository.insert(salary, salaryEntries), checkIn = true)
        assertEquals(month(11), vm.sheet.month)
        vm.onIntent(AccountDetailIntent.SheetDismissed)
        vm.onIntent(AccountDetailIntent.Shown)
        assertNull(vm.state.value.sheet)
    }

    @Test
    fun investmentHasNoCheckIn() {
        val brokerage = FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), today)
        val vm = open(repository.insert(brokerage, trades = true))
        assertNull(vm.loaded.timeline)
        assertFalse(vm.loaded.canCheckIn)
        assertEquals("0.00", vm.loaded.header.balance)
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        assertNull(vm.state.value.sheet)
        assertIs<Period.Transactions>(vm.loaded.account.period)
    }

    @Test
    fun checkInSaveAndDeleteReportAChange() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        val effects = effectsOf(vm)
        vm.onIntent(AccountDetailIntent.CheckInTapped)
        vm.onIntent(AccountDetailIntent.AmountChanged("1"))
        vm.onIntent(AccountDetailIntent.SaveTapped)
        vm.onIntent(AccountDetailIntent.MonthTapped(month(10)))
        vm.onIntent(AccountDetailIntent.DeleteTapped)
        vm.onIntent(AccountDetailIntent.DeleteConfirmed)
        assertEquals(listOf(AccountDetailEffect.AccountChanged, AccountDetailEffect.AccountChanged), effects)
    }

    @Test
    fun editOpensWithNameAndCurrentBalance() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.EditTapped)
        assertEquals("Salary", vm.edit.name)
        assertEquals("12340.00", vm.edit.amount)
        assertEquals("12,340.00", vm.edit.amountText)
        assertTrue(vm.edit.showsAmount)
        assertTrue(vm.edit.canSave)
    }

    @Test
    fun editOfAnInvestmentHasNoAmount() {
        val brokerage = FinanceAccount("Brokerage", Currency.EUR, AccountDetails.InvestmentDetails(InvestmentKind.StocksEtf, "XTB"), today)
        val vm = open(repository.insert(brokerage, trades = true))
        vm.onIntent(AccountDetailIntent.EditTapped)
        assertFalse(vm.edit.showsAmount)
        assertTrue(vm.edit.canSave)
    }

    @Test
    fun dismissingTheEditChangesNothing() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditNameChanged("Daily"))
        vm.onIntent(AccountDetailIntent.EditDismissed)
        assertNull(vm.state.value.edit)
        assertEquals("Salary", vm.loaded.header.name)
        assertEquals(0, repository.editCalls)
    }

    @Test
    fun blankNameOrAmountDisablesSave() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditNameChanged("  "))
        assertFalse(vm.edit.canSave)
        vm.onIntent(AccountDetailIntent.EditNameChanged("Daily"))
        vm.onIntent(AccountDetailIntent.EditAmountChanged(""))
        assertFalse(vm.edit.canSave)
        vm.onIntent(AccountDetailIntent.EditAmountChanged("12a3,456"))
        assertEquals("123.45", vm.edit.amount)
    }

    @Test
    fun savedEditUpdatesTheScreenAndReportsIt() = runTest {
        // November not checked in: it carries October's 10,810.00 forward.
        val vm = open(repository.insert(salary, salaryEntries.dropLast(1)))
        val effects = effectsOf(vm)
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditNameChanged("  Daily card "))
        vm.onIntent(AccountDetailIntent.EditAmountChanged("12340"))
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        assertNull(vm.state.value.edit)
        assertEquals("Daily card", vm.loaded.header.name)
        assertEquals("12,340.00", vm.loaded.header.balance)
        assertTrue(vm.loaded.timeline!!.first().checkedIn)
        assertEquals(listOf(AccountDetailEffect.AccountChanged), effects)
    }

    @Test
    fun renameOnlyKeepsACarriedMonthUnchecked() {
        val vm = open(repository.insert(salary, salaryEntries.dropLast(1)))
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditNameChanged("Daily card"))
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        assertEquals("Daily card", vm.loaded.header.name)
        assertFalse(vm.loaded.timeline!!.first().checkedIn)
    }

    @Test
    fun unchangedEditStoresNothing() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        val effects = effectsOf(vm)
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        assertNull(vm.state.value.edit)
        assertEquals(0, repository.editCalls)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun failedEditKeepsTheSheet() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.EditTapped)
        repository.failEdit = true
        vm.onIntent(AccountDetailIntent.EditNameChanged("Daily"))
        vm.onIntent(AccountDetailIntent.EditAmountChanged("3000"))
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        assertTrue(vm.edit.failed)
        assertEquals("Couldn't save the changes. Try again.", EDIT_SAVE_FAILED_MESSAGE)
        assertEquals("Daily", vm.edit.name)
        assertEquals("3000", vm.edit.amount)
        assertFalse(vm.edit.saving)
        assertEquals("Salary", vm.loaded.header.name)
        assertEquals("12,340.00", vm.loaded.header.balance)
    }

    @Test
    fun doubleTapEditSavesOnce() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.EditNameChanged("Daily"))
        val gate = CompletableDeferred<Unit>()
        repository.editGate = gate
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        vm.onIntent(AccountDetailIntent.EditSaveTapped)
        assertTrue(vm.edit.saving)
        gate.complete(Unit)
        assertEquals(1, repository.editCalls)
    }

    @Test
    fun deleteAccountAsksThenCancels() {
        val vm = open(repository.insert(salary, salaryEntries))
        vm.onIntent(AccountDetailIntent.DeleteAccountTapped)
        assertTrue(vm.state.value.confirmDeleteAccount)
        vm.onIntent(AccountDetailIntent.DeleteAccountCancelled)
        assertFalse(vm.state.value.confirmDeleteAccount)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun confirmedDeleteReportsTheNeighbour() = runTest {
        repository.insert(salary)
        val middle = repository.insert(salary.copy(name = "Brokerage"))
        val last = repository.insert(salary.copy(name = "Car loan"))
        val vm = open(middle)
        val effects = effectsOf(vm)
        vm.onIntent(AccountDetailIntent.DeleteAccountTapped)
        vm.onIntent(AccountDetailIntent.DeleteAccountConfirmed)
        assertEquals(listOf<AccountDetailEffect>(AccountDetailEffect.AccountDeleted(last.id)), effects)
        assertEquals(2, repository.saved.size)
    }

    @Test
    fun failedDeleteKeepsTheAccount() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        val effects = effectsOf(vm)
        repository.failDeleteAccount = true
        vm.onIntent(AccountDetailIntent.DeleteAccountTapped)
        vm.onIntent(AccountDetailIntent.DeleteAccountConfirmed)
        assertEquals(listOf<AccountDetailEffect>(AccountDetailEffect.DeleteFailed), effects)
        assertFalse(vm.state.value.deleting)
        assertEquals("Salary", vm.loaded.header.name)
        assertEquals(1, repository.saved.size)
    }

    @Test
    fun deleteInProgressBlocksEditAndAnotherDelete() = runTest {
        val vm = open(repository.insert(salary, salaryEntries))
        val gate = CompletableDeferred<Unit>()
        repository.deleteGate = gate
        vm.onIntent(AccountDetailIntent.DeleteAccountTapped)
        vm.onIntent(AccountDetailIntent.DeleteAccountConfirmed)
        assertTrue(vm.state.value.deleting)
        vm.onIntent(AccountDetailIntent.EditTapped)
        vm.onIntent(AccountDetailIntent.DeleteAccountTapped)
        vm.onIntent(AccountDetailIntent.DeleteAccountConfirmed)
        assertNull(vm.state.value.edit)
        assertFalse(vm.state.value.confirmDeleteAccount)
        gate.complete(Unit)
        assertEquals(1, repository.deleteCalls)
    }
}
