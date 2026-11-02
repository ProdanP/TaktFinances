import Foundation
import SharedLogic

struct AddAccountState {
    var type: AccountType?
    var name = ""
    var currency: Currency?
    var amountText = ""
    var problems: [AccountDraftProblem] = []
    var unavailable = false
    var didSave = false
}

enum AddAccountIntent {
    case typeSelected(AccountType)
    case nameChanged(String)
    case currencySelected(Currency)
    case amountChanged(String)
    case saveTapped
}

@Observable
final class AddAccountModel {
    private(set) var state = AddAccountState()
    private let repository: AccountRepository

    init(repository: AccountRepository = KoinInjector.shared.accountRepository) {
        self.repository = repository
    }

    func send(_ intent: AddAccountIntent) async {
        switch intent {
        case let .typeSelected(type):
            state.type = type
            state.problems = []
        case let .nameChanged(value):
            state.name = value
            state.problems = []
        case let .currencySelected(currency):
            state.currency = currency
            state.problems = []
        case let .amountChanged(value):
            state.amountText = value
            state.problems = []
        case .saveTapped:
            await save()
        }
    }

    private func save() async {
        let canonical = canonicalAmount(from: state.amountText)
        let units = MinorUnitsKt.minorUnits(canonical: canonical)?.int64Value
        let problems = AccountDraftProblemsKt.accountDraftProblems(
            name: state.name,
            type: state.type,
            currency: state.currency,
            minorUnits: units.map { KotlinLong(value: $0) }
        )
        if !problems.isEmpty {
            state.problems = problems
            return
        }
        guard let type = state.type,
              let currency = state.currency,
              let units else {
            return
        }
        let money = Money(minorUnits: units, currency: currency)
        let draft = NewAccount(
            name: state.name.trimmingCharacters(in: .whitespacesAndNewlines),
            type: type,
            balance: money
        )
        do {
            let outcome = try await repository.add(account: draft)
            guard outcome is OutcomeOk else {
                state.unavailable = true
                return
            }
            state.didSave = true
            state.unavailable = false
        } catch {
            state.unavailable = true
        }
    }

    private func canonicalAmount(from text: String) -> String {
        var seenDecimal = false
        var result = ""
        for character in text {
            if character.isNumber {
                result.append(character)
            } else if (character == "." || character == ",") && !seenDecimal {
                result.append(".")
                seenDecimal = true
            }
        }
        let parts = result.split(separator: ".", maxSplits: 1, omittingEmptySubsequences: false)
        if parts.count == 1 {
            return String(parts[0])
        }
        return "\(parts[0]).\(parts[1].prefix(2))"
    }
}
