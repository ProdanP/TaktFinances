import Foundation
import SharedLogic

struct AddAccountState {
    var types: [AccountType] = []
    var currencies: [Currency] = []
    var type: AccountType = .bankaccount
    var currency: Currency = CurrencyKt.defaultCurrency()
    var name: String = ""
    var amount: String = ""
    var nameInvalid: Bool = false
    var amountInvalid: Bool = false
    var saved: Bool = false
}

enum AddAccountIntent {
    case typeSelected(AccountType)
    case currencySelected(Currency)
    case nameChanged(String)
    case amountChanged(String)
    case saveTapped
}

@Observable
final class AddAccountModel {
    private(set) var state = AddAccountState()
    private let addAccount: AddAccount

    init(repository: AccountRepository) {
        addAccount = AddAccount(repository: repository)
        state.types = AccountTypeKt.accountTypes()
        state.currencies = CurrencyKt.currencies()
    }

    func send(_ intent: AddAccountIntent) {
        switch intent {
        case let .typeSelected(type):
            state.type = type
        case let .currencySelected(currency):
            state.currency = currency
            state.amountInvalid = false
        case let .nameChanged(value):
            state.name = value
            state.nameInvalid = false
        case let .amountChanged(value):
            state.amount = value
            state.amountInvalid = false
        case .saveTapped:
            save()
        }
    }

    private func save() {
        let result = addAccount.invoke(
            type: state.type,
            name: state.name,
            currency: state.currency,
            amount: state.amount
        )
        if let invalid = result as? AddAccountResultInvalid {
            state.nameInvalid = invalid.nameInvalid
            state.amountInvalid = invalid.amountInvalid
        } else if result is AddAccountResultCreated {
            state.saved = true
        }
    }
}
