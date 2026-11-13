#if DEBUG
import SharedLogic

/// In-memory repository for SwiftUI previews, which do not open the app database.
final class PreviewAccountRepository: AccountRepository {
    private var saved: [SavedAccount]

    init(_ accounts: [FinanceAccount] = []) {
        saved = accounts.enumerated().map { SavedAccount(id: Int64($0.offset + 1), account: $0.element) }
    }

    func add(account: FinanceAccount, completionHandler: @escaping (Outcome?, Error?) -> Void) {
        let item = SavedAccount(id: Int64(saved.count + 1), account: account)
        saved.append(item)
        completionHandler(OutcomeOk(value: item), nil)
    }

    func all(completionHandler: @escaping (Outcome?, Error?) -> Void) {
        completionHandler(OutcomeOk(value: saved as NSArray), nil)
    }
}
#endif
