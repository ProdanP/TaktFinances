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

    var banks: [Bank] = []
    var bankAccountKinds: [BankAccountKind] = []
    var investmentKinds: [InvestmentKind] = []
    var loanDirections: [LoanDirection] = []
    var details = DetailsInput()
    var detailErrors = DetailErrorFlags()
}

struct DetailErrorFlags {
    var bank = false
    var accountKind = false
    var last4 = false
    var investmentKind = false
    var platform = false
    var direction = false
    var counterparty = false

    init() {}

    init(_ errors: DetailErrors) {
        bank = errors.bank
        accountKind = errors.accountKind
        last4 = errors.last4
        investmentKind = errors.investmentKind
        platform = errors.platform
        direction = errors.direction
        counterparty = errors.counterparty
    }
}

struct DetailsInput {
    var bank: Bank?
    var bankAccountKind: BankAccountKind?
    var last4: String = ""
    var investmentKind: InvestmentKind?
    var platform: String = ""
    var loanDirection: LoanDirection?
    var counterparty: String = ""

    var draft: AccountDetailsDraft {
        AccountDetailsDraft(
            bank: bank,
            bankAccountKind: bankAccountKind,
            last4: last4,
            investmentKind: investmentKind,
            platform: platform,
            loanDirection: loanDirection,
            counterparty: counterparty
        )
    }
}

enum AddAccountIntent {
    case typeSelected(AccountType)
    case currencySelected(Currency)
    case nameChanged(String)
    case amountChanged(String)
    case bankSelected(Bank)
    case bankAccountKindSelected(BankAccountKind)
    case last4Changed(String)
    case investmentKindSelected(InvestmentKind)
    case platformChanged(String)
    case loanDirectionSelected(LoanDirection)
    case counterpartyChanged(String)
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
        state.banks = BankKt.banks()
        state.bankAccountKinds = AccountKindsKt.bankAccountKinds()
        state.investmentKinds = AccountKindsKt.investmentKinds()
        state.loanDirections = AccountKindsKt.loanDirections()
    }

    func send(_ intent: AddAccountIntent) {
        switch intent {
        case let .typeSelected(type):
            state.type = type
            state.details = DetailsInput()
            state.detailErrors = DetailErrorFlags()
        case let .currencySelected(currency):
            state.currency = currency
            state.amountInvalid = false
        case let .nameChanged(value):
            state.name = value
            state.nameInvalid = false
        case let .amountChanged(value):
            state.amount = value
            state.amountInvalid = false
        case let .bankSelected(bank):
            state.details.bank = bank
            state.detailErrors.bank = false
        case let .bankAccountKindSelected(kind):
            state.details.bankAccountKind = kind
            state.detailErrors.accountKind = false
        case let .last4Changed(value):
            state.details.last4 = String(value.filter { $0.isASCII && $0.isNumber }.prefix(4))
            state.detailErrors.last4 = false
        case let .investmentKindSelected(kind):
            state.details.investmentKind = kind
            state.detailErrors.investmentKind = false
        case let .platformChanged(value):
            state.details.platform = value
            state.detailErrors.platform = false
        case let .loanDirectionSelected(direction):
            state.details.loanDirection = direction
            state.detailErrors.direction = false
        case let .counterpartyChanged(value):
            state.details.counterparty = value
            state.detailErrors.counterparty = false
        case .saveTapped:
            save()
        }
    }

    private func save() {
        let result = addAccount.invoke(
            type: state.type,
            name: state.name,
            currency: state.currency,
            amount: state.amount,
            details: state.details.draft
        )
        if let invalid = result as? AddAccountResultInvalid {
            state.nameInvalid = invalid.nameInvalid
            state.amountInvalid = invalid.amountInvalid
            state.detailErrors = DetailErrorFlags(invalid.detailErrors)
        } else if result is AddAccountResultCreated {
            state.saved = true
        }
    }
}
