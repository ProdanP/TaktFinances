#if DEBUG
import SharedLogic

/// In-memory repository for SwiftUI previews, which do not open the app database.
final class PreviewAccountRepository: AccountRepository {
    private var saved: [SavedAccount] = []

    func add(account: FinanceAccount, start: StartingPointPayload, completionHandler: @escaping (Outcome?, Error?) -> Void) {
        let id = Int64(saved.count + 1)
        let period: Period
        if let monthly = start as? StartingPointPayloadMonthly {
            period = PeriodMonthly(id: id, start: account.createdOn, entries: [monthly.entry])
        } else {
            period = PeriodTransactions(id: id, start: account.createdOn, openingBalanceMinor: 0)
        }
        let item = SavedAccount(id: id, account: account, period: period)
        saved.append(item)
        completionHandler(OutcomeOk(value: item), nil)
    }

    func all(completionHandler: @escaping (Outcome?, Error?) -> Void) {
        completionHandler(OutcomeOk(value: saved as NSArray), nil)
    }

    func get(id: Int64, completionHandler: @escaping (Outcome?, Error?) -> Void) {
        if let item = saved.first(where: { $0.id == id }) {
            completionHandler(OutcomeOk(value: item), nil)
        } else {
            completionHandler(OutcomeFailed(reason: ReasonUnavailable.shared), nil)
        }
    }

    func saveMonthlyEntry(periodId: Int64, entry: MonthlyEntry, completionHandler: @escaping (Outcome?, Error?) -> Void) {
        completionHandler(OutcomeOk(value: NSNull()), nil)
    }

    func deleteMonthlyEntry(periodId: Int64, month: Kotlinx_datetimeYearMonth, completionHandler: @escaping (Outcome?, Error?) -> Void) {
        completionHandler(OutcomeOk(value: NSNull()), nil)
    }
}
#endif
