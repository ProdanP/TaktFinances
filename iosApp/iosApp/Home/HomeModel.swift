import Foundation
import SharedLogic

struct HomeState {
    var destinations: [HomeDestination] = []
    var selected: HomeDestination = .dashboard
    var finances: MyFinancesContent = .loading
    /// Each account's current balance by id, from the last successful read.
    var balances: [Int64: Int64] = [:]
}

/// What "Recent activity" shows for the selected account.
enum RecentActivity {
    /// Newest first, at most five: month label and formatted balance; followed by "See all".
    case entries([(month: String, amount: String)], currency: String)
    /// A Monthly account with only its start month: "No check-ins yet" and a "Check in" action.
    case noCheckIns
    /// An account tracked by trades that has none yet: "No trades yet", no action.
    case noTrades

    init(_ saved: SavedAccount) {
        guard let period = saved.period as? PeriodMonthly else {
            self = .noTrades
            return
        }
        guard period.hasCheckIns else {
            self = .noCheckIns
            return
        }
        let rows = period.recentEntries(limit: TrackingKt.RECENT_ENTRIES).map {
            (month: ChangeFormatKt.monthLabel(month: $0.month), amount: AmountInputKt.formatAmount(minor: $0.amountMinor))
        }
        self = .entries(rows, currency: saved.account.currency.code)
    }
}

/// What the My Finances tab shows.
enum MyFinancesContent {
    /// The first read has not finished yet.
    case loading
    /// The accounts could not be read; the user can retry.
    case failed
    /// No account exists yet.
    case empty
    /// Accounts oldest first; `selectedId` is one of their ids.
    case accounts([SavedAccount], selectedId: Int64)
}

enum HomeIntent {
    case destinationSelected(HomeDestination)
    /// My Finances became visible (first appearance, or returning from Add Account or Account Detail).
    case shown
    case accountSelected(Int64)
    case previousTapped
    case nextTapped
    case retryTapped
}

@Observable
final class HomeModel {
    private(set) var state = HomeState()
    private let repository: AccountRepository
    /// Ids from the last successful read; nil before the first one.
    private var knownIds: Set<Int64>?

    init(repository: AccountRepository) {
        self.repository = repository
        let destinations = HomeDestinationKt.homeDestinations()
        state = HomeState(
            destinations: destinations,
            selected: destinations.first ?? .dashboard
        )
    }

    func send(_ intent: HomeIntent) {
        switch intent {
        case let .destinationSelected(destination):
            state.selected = destination
        case .shown, .retryTapped:
            reload()
        case let .accountSelected(id):
            select { accounts, _ in accounts.first { $0.id == id } }
        case .previousTapped:
            select { accounts, index in index > 0 ? accounts[index - 1] : nil }
        case .nextTapped:
            select { accounts, index in index + 1 < accounts.count ? accounts[index + 1] : nil }
        }
    }

    private func reload() {
        Task { @MainActor in
            let outcome = try? await repository.all()
            guard let ok = outcome as? OutcomeOk<AnyObject>, let list = ok.value as? [SavedAccount] else {
                state.finances = .failed
                return
            }
            let today = TodayKt.today()
            state.balances = Dictionary(uniqueKeysWithValues: list.map { ($0.id, $0.balanceMinor(today: today)) })
            state.finances = content(for: list)
        }
    }

    /// A newly added account is selected; otherwise the previous selection is kept while it
    /// still exists. When it was deleted, its neighbour is selected; the first card only when
    /// nothing was selected before.
    private func content(for list: [SavedAccount]) -> MyFinancesContent {
        let previousIds = knownIds
        knownIds = Set(list.map(\.id))
        guard let first = list.first else { return .empty }
        var previousSelection: Int64?
        if case let .accounts(_, selectedId) = state.finances { previousSelection = selectedId }
        let added = previousIds.map { ids in list.filter { !ids.contains($0.id) } } ?? []
        let selectedId: Int64
        if let newest = added.map(\.id).max() {
            selectedId = newest
        } else if let previousSelection, list.contains(where: { $0.id == previousSelection }) {
            selectedId = previousSelection
        } else if let previousSelection,
                  let neighbour = NeighbourKt.neighbourAfterRemoving(remaining: list, removedId: previousSelection) {
            selectedId = neighbour.id
        } else {
            selectedId = first.id
        }
        return .accounts(list, selectedId: selectedId)
    }

    private func select(_ pick: ([SavedAccount], Int) -> SavedAccount?) {
        guard case let .accounts(accounts, selectedId) = state.finances else { return }
        let index = accounts.firstIndex { $0.id == selectedId } ?? 0
        guard let target = pick(accounts, index) else { return }
        state.finances = .accounts(accounts, selectedId: target.id)
    }
}
