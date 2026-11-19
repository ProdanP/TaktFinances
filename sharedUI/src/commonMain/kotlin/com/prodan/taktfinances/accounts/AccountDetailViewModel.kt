package com.prodan.taktfinances.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.entity.Period
import com.prodan.taktfinances.accounts.domain.entity.amountInputText
import com.prodan.taktfinances.accounts.domain.entity.balanceMinor
import com.prodan.taktfinances.accounts.domain.entity.canDeleteEntry
import com.prodan.taktfinances.accounts.domain.entity.checkInMonths
import com.prodan.taktfinances.accounts.domain.entity.entryFor
import com.prodan.taktfinances.accounts.domain.entity.filterAmountInput
import com.prodan.taktfinances.accounts.domain.entity.formatAmount
import com.prodan.taktfinances.accounts.domain.entity.monthEndBalances
import com.prodan.taktfinances.accounts.domain.entity.monthLabel
import com.prodan.taktfinances.accounts.domain.entity.previousMonth
import com.prodan.taktfinances.accounts.domain.entity.shortMonthLabel
import com.prodan.taktfinances.accounts.domain.entity.today
import com.prodan.taktfinances.accounts.domain.usecase.CheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.CheckInResult
import com.prodan.taktfinances.accounts.domain.usecase.DeleteCheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.DeleteCheckInResult
import com.prodan.taktfinances.accounts.domain.usecase.DeleteAccountResult
import com.prodan.taktfinances.accounts.domain.usecase.DeleteAccountUseCase
import com.prodan.taktfinances.accounts.domain.usecase.EditAccountResult
import com.prodan.taktfinances.accounts.domain.usecase.EditAccountUseCase
import com.prodan.taktfinances.accounts.models.AccountDetailContent
import com.prodan.taktfinances.accounts.models.CheckInSheetModel
import com.prodan.taktfinances.accounts.models.EditSheetModel
import com.prodan.taktfinances.accounts.models.MonthOption
import com.prodan.taktfinances.accounts.models.SheetError
import com.prodan.taktfinances.accounts.models.toHeader
import com.prodan.taktfinances.accounts.models.toTimeline
import com.prodan.taktfinances.core.Outcome
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/**
 * Account Detail for [accountId]. [checkInOnOpen] opens the check-in sheet for the current month after
 * the first successful load. [today] is read on each load and save; tests pass a fixed date.
 */
internal class AccountDetailViewModel(
    private val accountId: Long,
    private val checkInOnOpen: Boolean,
    private val repository: AccountRepository,
    private val checkIn: CheckInUseCase,
    private val deleteCheckIn: DeleteCheckInUseCase,
    private val editAccount: EditAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val today: () -> LocalDate = ::today,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountDetailState())
    val state: StateFlow<AccountDetailState> = _state.asStateFlow()

    private val _effects = Channel<AccountDetailEffect>(Channel.BUFFERED)
    val effects: Flow<AccountDetailEffect> = _effects.receiveAsFlow()

    private var pendingCheckIn = checkInOnOpen

    fun onIntent(intent: AccountDetailIntent) {
        when (intent) {
            AccountDetailIntent.Shown, AccountDetailIntent.RetryTapped -> load()
            is AccountDetailIntent.RangeSelected -> _state.update { it.copy(range = intent.range) }
            AccountDetailIntent.CheckInTapped -> openSheet(today().yearMonth)
            is AccountDetailIntent.MonthTapped -> openSheet(intent.month)
            is AccountDetailIntent.SheetMonthSelected -> openSheet(intent.month)
            is AccountDetailIntent.AmountChanged -> updateSheet { copy(amount = filterAmountInput(intent.value), error = null) }
            is AccountDetailIntent.AmountFocusChanged -> updateSheet { copy(amountFocused = intent.focused) }
            AccountDetailIntent.SameAsLastMonthTapped -> updateSheet { copy(amount = previousAmount ?: amount, error = null) }
            AccountDetailIntent.SaveTapped -> save()
            AccountDetailIntent.DeleteTapped -> updateSheet { if (canDelete) copy(confirmDelete = true, error = null) else this }
            AccountDetailIntent.DeleteCancelled -> updateSheet { copy(confirmDelete = false) }
            AccountDetailIntent.DeleteConfirmed -> delete()
            AccountDetailIntent.SheetDismissed -> _state.update { if (it.sheet?.saving == true) it else it.copy(sheet = null) }
            AccountDetailIntent.EditTapped -> openEdit()
            is AccountDetailIntent.EditNameChanged -> updateEdit { copy(name = intent.value, failed = false) }
            is AccountDetailIntent.EditAmountChanged -> updateEdit { copy(amount = filterAmountInput(intent.value), failed = false) }
            is AccountDetailIntent.EditAmountFocusChanged -> updateEdit { copy(amountFocused = intent.focused) }
            AccountDetailIntent.EditSaveTapped -> saveEdit()
            AccountDetailIntent.EditDismissed -> _state.update { if (it.edit?.saving == true) it else it.copy(edit = null) }
            AccountDetailIntent.DeleteAccountTapped ->
                _state.update { if (it.content is AccountDetailContent.Loaded && !it.deleting) it.copy(confirmDeleteAccount = true) else it }
            AccountDetailIntent.DeleteAccountCancelled -> _state.update { it.copy(confirmDeleteAccount = false) }
            AccountDetailIntent.DeleteAccountConfirmed -> removeAccount()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val content = when (val outcome = repository.get(accountId)) {
                is Outcome.Failed -> AccountDetailContent.Failed
                is Outcome.Ok -> {
                    val account = outcome.value
                    val balances = monthEndBalances(account, today())
                    AccountDetailContent.Loaded(account, account.toHeader(balances), balances, account.toTimeline(balances))
                }
            }
            _state.update { it.copy(content = content) }
            if (content is AccountDetailContent.Loaded && pendingCheckIn) {
                pendingCheckIn = false
                openSheet(today().yearMonth)
            }
        }
    }

    /** Opens (or retargets) the sheet for [month], filled with its entry and the previous month's balance. */
    private fun openSheet(month: YearMonth) {
        val loaded = _state.value.content as? AccountDetailContent.Loaded ?: return
        val period = loaded.account.period as? Period.Monthly ?: return
        if (_state.value.sheet?.saving == true) return
        val months = checkInMonths(period, today())
        if (month !in months) return
        val previous = previousMonth(month).takeIf { month != period.startMonth }
        val previousBalance = previous?.let { p -> loaded.balances.firstOrNull { it.month == p }?.balanceMinor }
        _state.update {
            it.copy(
                sheet = CheckInSheetModel(
                    month = month,
                    monthLabel = monthLabel(month),
                    months = months.map { m -> MonthOption(m, monthLabel(m)) },
                    amount = period.entryFor(month)?.amountMinor?.let(::amountInputText).orEmpty(),
                    previousHint = previousBalance?.let { b -> "${shortMonthLabel(previous)}: ${formatAmount(b)}" },
                    previousAmount = previousBalance?.let(::amountInputText),
                    canDelete = period.canDeleteEntry(month),
                ),
            )
        }
    }

    private fun updateSheet(change: CheckInSheetModel.() -> CheckInSheetModel) {
        _state.update { state -> state.sheet?.let { state.copy(sheet = it.change()) } ?: state }
    }

    private fun save() {
        val sheet = _state.value.sheet ?: return
        val account = (_state.value.content as? AccountDetailContent.Loaded)?.account ?: return
        if (!sheet.canSave) return
        updateSheet { copy(saving = true, error = null) }
        viewModelScope.launch {
            when (checkIn(account, sheet.month, sheet.amount, today())) {
                CheckInResult.Saved -> {
                    _state.update { it.copy(sheet = null) }
                    load()
                    _effects.trySend(AccountDetailEffect.AccountChanged)
                }
                CheckInResult.Invalid -> updateSheet { copy(saving = false) }
                is CheckInResult.Failed -> updateSheet { copy(saving = false, error = SheetError.SaveFailed) }
            }
        }
    }

    private fun delete() {
        val sheet = _state.value.sheet ?: return
        val account = (_state.value.content as? AccountDetailContent.Loaded)?.account ?: return
        if (sheet.saving) return
        updateSheet { copy(saving = true, confirmDelete = false) }
        viewModelScope.launch {
            when (deleteCheckIn(account, sheet.month)) {
                DeleteCheckInResult.Deleted -> {
                    _state.update { it.copy(sheet = null) }
                    load()
                    _effects.trySend(AccountDetailEffect.AccountChanged)
                }
                DeleteCheckInResult.Invalid -> updateSheet { copy(saving = false) }
                is DeleteCheckInResult.Failed -> updateSheet { copy(saving = false, error = SheetError.DeleteFailed) }
            }
        }
    }

    /** Opens the "Edit account" sheet filled with the name and, for a Monthly account, the current balance. */
    private fun openEdit() {
        val state = _state.value
        val account = (state.content as? AccountDetailContent.Loaded)?.account ?: return
        if (state.deleting) return
        val monthly = account.period is Period.Monthly
        _state.update {
            it.copy(
                edit = EditSheetModel(
                    name = account.account.name,
                    amount = if (monthly) amountInputText(account.balanceMinor(today())) else "",
                    showsAmount = monthly,
                ),
            )
        }
    }

    private fun updateEdit(change: EditSheetModel.() -> EditSheetModel) {
        _state.update { state -> state.edit?.let { state.copy(edit = it.change()) } ?: state }
    }

    private fun saveEdit() {
        val state = _state.value
        val edit = state.edit ?: return
        val account = (state.content as? AccountDetailContent.Loaded)?.account ?: return
        if (!edit.canSave || state.deleting) return
        updateEdit { copy(saving = true, failed = false) }
        viewModelScope.launch {
            when (editAccount(account, edit.name, edit.amount, today())) {
                EditAccountResult.Saved -> {
                    _state.update { it.copy(edit = null) }
                    load()
                    _effects.trySend(AccountDetailEffect.AccountChanged)
                }
                EditAccountResult.Unchanged -> _state.update { it.copy(edit = null) }
                EditAccountResult.Invalid -> updateEdit { copy(saving = false) }
                is EditAccountResult.Failed -> updateEdit { copy(saving = false, failed = true) }
            }
        }
    }

    private fun removeAccount() {
        val state = _state.value
        if (state.deleting || state.content !is AccountDetailContent.Loaded) return
        _state.update { it.copy(confirmDeleteAccount = false, deleting = true) }
        viewModelScope.launch {
            when (val result = deleteAccount(accountId)) {
                is DeleteAccountResult.Deleted -> _effects.trySend(AccountDetailEffect.AccountDeleted(result.neighbourId))
                is DeleteAccountResult.Failed -> {
                    _state.update { it.copy(deleting = false) }
                    _effects.trySend(AccountDetailEffect.DeleteFailed)
                }
            }
        }
    }
}
