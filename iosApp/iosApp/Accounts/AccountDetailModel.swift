import Foundation
import SharedLogic

/// A signed change ready to show: "+1,530.00 (+14.2%)", its direction and a spoken label.
struct ChangeUI: Equatable {
    enum Direction { case up, down, flat }
    let text: String
    let direction: Direction
    let spoken: String

    init(_ change: MonthChange, currency: String) {
        let amount = change.amountMinor
        direction = amount > 0 ? .up : (amount < 0 ? .down : .flat)
        text = ChangeFormatKt.formatChange(change: change)
        let word = switch direction {
        case .up: "Increase of"
        case .down: "Decrease of"
        case .flat: "No change,"
        }
        var percent = ""
        if let tenths = change.percentTenths?.int64Value {
            percent = ", \(abs(tenths) / 10).\(abs(tenths) % 10) percent"
        }
        spoken = "\(word) \(AmountInputKt.formatAmount(minor: abs(amount))) \(currency)\(percent)"
    }
}

struct TimelineRow: Identifiable {
    let month: Kotlinx_datetimeYearMonth
    let label: String
    let balance: String
    /// Nil on the start month.
    let change: ChangeUI?
    let checkedIn: Bool
    var id: String { label }
}

struct ChartPoint: Identifiable {
    let index: Int
    let axisLabel: String
    let label: String
    let balance: Double
    let balanceText: String
    var id: Int { index }
}

enum ChartRange: String, CaseIterable {
    case sixMonths = "6M", oneYear = "1Y", all = "All"
    var months: Int? {
        switch self {
        case .sixMonths: 6
        case .oneYear: 12
        case .all: nil
        }
    }
}

struct LoadedAccount {
    let account: SavedAccount
    let balances: [MonthBalance]
    let currency: String
    let balance: String
    /// Nil when the account has no previous month ("This month —").
    let thisMonth: ChangeUI?
    /// Nil for an account tracked by trades ("No trades yet", no check-in).
    let timeline: [TimelineRow]?
}

enum AccountDetailContent {
    case loading
    case failed
    case loaded(LoadedAccount)
}

enum SheetError: String {
    case saveFailed = "Couldn't save the check-in. Try again."
    case deleteFailed = "Couldn't delete the check-in. Try again."
}

struct CheckInSheetState {
    var month: Kotlinx_datetimeYearMonth
    var monthLabel: String
    var months: [(month: Kotlinx_datetimeYearMonth, label: String)]
    var amount: String
    var amountFocused = false
    /// "October: 10,000.00"; nil on the start month.
    var previousHint: String?
    /// Raw input for "Same as last month"; nil on the start month.
    var previousAmount: String?
    var canDelete: Bool
    var saving = false
    var confirmDelete = false
    var error: SheetError?

    var canSave: Bool { ChangeFormatKt.isAmount(text: amount) && !saving }
    var amountText: String { amountFocused ? amount : (AmountInputKt.formatAmountInput(text: amount) ?? "") }
}

struct AccountDetailState {
    var content: AccountDetailContent = .loading
    var range: ChartRange = .sixMonths
    var sheet: CheckInSheetState?

    var chart: [ChartPoint] {
        guard case let .loaded(loaded) = content else { return [] }
        let shown = range.months.map { Array(loaded.balances.suffix($0)) } ?? loaded.balances
        return shown.enumerated().map { index, balance in
            ChartPoint(
                index: index,
                axisLabel: ChangeFormatKt.monthAbbreviation(month: balance.month),
                label: ChangeFormatKt.monthLabel(month: balance.month),
                balance: Double(balance.balanceMinor) / 100,
                balanceText: AmountInputKt.formatAmount(minor: balance.balanceMinor)
            )
        }
    }
}

enum AccountDetailIntent {
    case shown
    case retryTapped
    case rangeSelected(ChartRange)
    /// The "Check in" action: the sheet for the current month.
    case checkInTapped
    /// A timeline row or its "Check in" shortcut.
    case monthTapped(Kotlinx_datetimeYearMonth)
    case sheetMonthSelected(Kotlinx_datetimeYearMonth)
    case amountChanged(String)
    case amountFocusChanged(Bool)
    case sameAsLastMonthTapped
    case saveTapped
    case deleteTapped
    case deleteConfirmed
    case deleteCancelled
    case sheetDismissed
}

/// Account Detail for one account. The rules (month-end balances, months offered, what may be
/// deleted) come from SharedLogic; this model only keeps screen state.
@Observable
final class AccountDetailModel {
    private(set) var state = AccountDetailState()
    private let repository: AccountRepository
    private let accountId: Int64
    private let checkIn: CheckInUseCase
    private let deleteCheckIn: DeleteCheckInUseCase
    private var pendingCheckIn: Bool

    init(repository: AccountRepository, accountId: Int64, checkInOnOpen: Bool) {
        self.repository = repository
        self.accountId = accountId
        checkIn = CheckInUseCase(repository: repository)
        deleteCheckIn = DeleteCheckInUseCase(repository: repository)
        pendingCheckIn = checkInOnOpen
    }

    func send(_ intent: AccountDetailIntent) {
        switch intent {
        case .shown, .retryTapped:
            load()
        case let .rangeSelected(range):
            state.range = range
        case .checkInTapped:
            openSheet(currentMonth())
        case let .monthTapped(month), let .sheetMonthSelected(month):
            openSheet(month)
        case let .amountChanged(value):
            state.sheet?.amount = AmountInputKt.filterAmountInput(raw: value)
            state.sheet?.error = nil
        case let .amountFocusChanged(focused):
            state.sheet?.amountFocused = focused
        case .sameAsLastMonthTapped:
            if let previous = state.sheet?.previousAmount {
                state.sheet?.amount = previous
                state.sheet?.error = nil
            }
        case .saveTapped:
            save()
        case .deleteTapped:
            if state.sheet?.canDelete == true {
                state.sheet?.confirmDelete = true
                state.sheet?.error = nil
            }
        case .deleteCancelled:
            state.sheet?.confirmDelete = false
        case .deleteConfirmed:
            delete()
        case .sheetDismissed:
            if state.sheet?.saving != true { state.sheet = nil }
        }
    }

    private var loaded: LoadedAccount? {
        if case let .loaded(loaded) = state.content { return loaded }
        return nil
    }

    /// The newest month a check-in may use is the current one.
    private func currentMonth() -> Kotlinx_datetimeYearMonth? {
        guard let period = loaded?.account.period as? PeriodMonthly else { return nil }
        return MonthBalanceKt.checkInMonths(period: period, today: TodayKt.today()).first
    }

    private func load() {
        Task { @MainActor in
            let outcome = try? await repository.get(id: accountId)
            guard let ok = outcome as? OutcomeOk<AnyObject>, let account = ok.value as? SavedAccount else {
                state.content = .failed
                return
            }
            state.content = .loaded(Self.build(account))
            if pendingCheckIn {
                pendingCheckIn = false
                openSheet(currentMonth())
            }
        }
    }

    private static func build(_ account: SavedAccount) -> LoadedAccount {
        let currency = account.account.currency.code
        let balances = MonthBalanceKt.monthEndBalances(account: account, today: TodayKt.today())
        let changes = MonthBalanceKt.monthChanges(balances: balances).map { ($0 as? MonthChange).map { ChangeUI($0, currency: currency) } }
        var timeline: [TimelineRow]?
        if account.period is PeriodMonthly {
            timeline = balances.indices.reversed().map { i in
                TimelineRow(
                    month: balances[i].month,
                    label: ChangeFormatKt.monthLabel(month: balances[i].month),
                    balance: AmountInputKt.formatAmount(minor: balances[i].balanceMinor),
                    change: changes[i],
                    checkedIn: balances[i].checkedIn
                )
            }
        }
        return LoadedAccount(
            account: account,
            balances: balances,
            currency: currency,
            balance: AmountInputKt.formatAmount(minor: balances.last?.balanceMinor ?? 0),
            thisMonth: changes.last ?? nil,
            timeline: timeline
        )
    }

    /// Opens (or retargets) the sheet for `month`, filled with its entry and the previous month's balance.
    private func openSheet(_ month: Kotlinx_datetimeYearMonth?) {
        guard let month, let loaded, let period = loaded.account.period as? PeriodMonthly else { return }
        guard state.sheet?.saving != true else { return }
        let months = MonthBalanceKt.checkInMonths(period: period, today: TodayKt.today())
        guard months.contains(month) else { return }
        var previousHint: String?
        var previousAmount: String?
        if month != period.startMonth {
            let previous = MonthBalanceKt.previousMonth(month: month)
            if let balance = loaded.balances.first(where: { $0.month == previous })?.balanceMinor {
                previousHint = "\(ChangeFormatKt.shortMonthLabel(month: previous)): \(AmountInputKt.formatAmount(minor: balance))"
                previousAmount = AmountInputKt.amountInputText(minor: balance)
            }
        }
        state.sheet = CheckInSheetState(
            month: month,
            monthLabel: ChangeFormatKt.monthLabel(month: month),
            months: months.map { (month: $0, label: ChangeFormatKt.monthLabel(month: $0)) },
            amount: period.entryFor(month: month).map { AmountInputKt.amountInputText(minor: $0.amountMinor) } ?? "",
            previousHint: previousHint,
            previousAmount: previousAmount,
            canDelete: period.canDeleteEntry(month: month)
        )
    }

    private func save() {
        guard let sheet = state.sheet, let account = loaded?.account, sheet.canSave else { return }
        state.sheet?.saving = true
        state.sheet?.error = nil
        Task { @MainActor in
            let result = try? await checkIn.invoke(account: account, month: sheet.month, amount: sheet.amount, today: TodayKt.today())
            if result is CheckInResultSaved {
                state.sheet = nil
                load()
            } else {
                state.sheet?.saving = false
                if !(result is CheckInResultInvalid) { state.sheet?.error = .saveFailed }
            }
        }
    }

    private func delete() {
        guard let sheet = state.sheet, let account = loaded?.account, !sheet.saving else { return }
        state.sheet?.saving = true
        state.sheet?.confirmDelete = false
        Task { @MainActor in
            let result = try? await deleteCheckIn.invoke(account: account, month: sheet.month)
            if result is DeleteCheckInResultDeleted {
                state.sheet = nil
                load()
            } else {
                state.sheet?.saving = false
                if !(result is DeleteCheckInResultInvalid) { state.sheet?.error = .deleteFailed }
            }
        }
    }
}
