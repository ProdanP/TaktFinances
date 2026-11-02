import Foundation
import SharedLogic

struct AccountsState {
    var accounts: [Account] = []
    var unavailable = false
    var showAddAccount = false
}

enum AccountsIntent {
    case opened
    case addTapped
    case dismissAdd
}

@Observable
final class AccountsModel {
    private(set) var state = AccountsState()
    private let repository: AccountRepository

    init(repository: AccountRepository = KoinInjector.shared.accountRepository) {
        self.repository = repository
    }

    func send(_ intent: AccountsIntent) async {
        switch intent {
        case .opened:
            await refresh()
        case .addTapped:
            state.showAddAccount = true
        case .dismissAdd:
            state.showAddAccount = false
            await refresh()
        }
    }

    private func refresh() async {
        do {
            let outcome = try await repository.accounts()
            guard let found = outcome as? OutcomeOk else {
                state.unavailable = true
                return
            }
            guard let list = found.value as? [Account] else {
                state.unavailable = true
                return
            }
            state.accounts = list
            state.unavailable = false
        } catch {
            state.unavailable = true
        }
    }
}
