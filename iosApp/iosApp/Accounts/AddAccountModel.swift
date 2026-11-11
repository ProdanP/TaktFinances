import Foundation
import SharedLogic

struct AddAccountState {
    var types: [AccountType] = []
    var currencies: [Currency] = []
    var type: AccountType = .bankaccount
    var currency: Currency = CurrencyKt.defaultCurrency()
    var name: String = ""
    var amount: String = ""
    var saved: Bool = false

    var banks: [Bank] = []
    var bankAccountKinds: [BankAccountKind] = []
    var investmentKinds: [InvestmentKind] = []
    var loanDirections: [LoanDirection] = []
    var details = DetailsInput()

    /// Field that currently has input focus.
    var focused: FormField?
    /// Fields edited since they last lost focus.
    var edited: Set<FormField> = []
    /// Fields the user edited and then left; only these may show an error.
    var left: Set<FormField> = []

    /// Shared check of the current input; refreshed by the model after every intent.
    fileprivate(set) var check = FormCheck(invalid: [])

    var visibleErrors: Set<FormField> { check.invalid.intersection(left) }
    var canSave: Bool { check.canSave }
    var hint: String? { check.hint }

    /// Raw input while the amount is edited, formatted ("2,500.00") otherwise; empty when blank.
    var amountText: String {
        focused == .amount ? amount : (AmountInputKt.formatAmountInput(text: amount) ?? "")
    }

    var summary: String? { AccountSummaryKt.accountSummary(type: type, details: details.draft) }
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
    case fieldFocusChanged(FormField, Bool)
    case saveTapped
}

private let detailFields: Set<FormField> = [
    .bank, .accountkind, .last4, .investmentkind, .platform, .direction, .counterparty,
]

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
        refreshCheck()
    }

    func send(_ intent: AddAccountIntent) {
        switch intent {
        case let .typeSelected(type):
            state.type = type
            state.details = DetailsInput()
            state.edited.subtract(detailFields)
            state.left.subtract(detailFields)
        case let .currencySelected(currency):
            state.currency = currency
        case let .nameChanged(value):
            state.name = value
            typed(.name)
        case let .amountChanged(value):
            state.amount = AmountInputKt.filterAmountInput(raw: value)
            typed(.amount)
        case let .bankSelected(bank):
            state.details.bank = bank
            state.left.insert(.bank)
        case let .bankAccountKindSelected(kind):
            state.details.bankAccountKind = kind
            state.left.insert(.accountkind)
        case let .last4Changed(value):
            state.details.last4 = String(value.filter { $0.isASCII && $0.isNumber }.prefix(4))
            typed(.last4)
        case let .investmentKindSelected(kind):
            state.details.investmentKind = kind
            state.left.insert(.investmentkind)
        case let .platformChanged(value):
            state.details.platform = value
            typed(.platform)
        case let .loanDirectionSelected(direction):
            state.details.loanDirection = direction
            state.left.insert(.direction)
        case let .counterpartyChanged(value):
            state.details.counterparty = value
            typed(.counterparty)
        case let .fieldFocusChanged(field, focused):
            focusChanged(field, focused: focused)
        case .saveTapped:
            save()
        }
        refreshCheck()
    }

    /// A text edit: the field's error hides until the user leaves it again.
    private func typed(_ field: FormField) {
        state.edited.insert(field)
        state.left.remove(field)
    }

    private func focusChanged(_ field: FormField, focused: Bool) {
        if focused {
            state.focused = field
            return
        }
        // Focus events can arrive in either order, so leaving never depends on which field is focused now.
        if state.focused == field { state.focused = nil }
        if state.edited.remove(field) != nil { state.left.insert(field) }
    }

    private func refreshCheck() {
        state.check = FormCheckKt.checkAccountForm(
            type: state.type,
            name: state.name,
            amount: state.amount,
            details: state.details.draft
        )
    }

    private func save() {
        guard state.canSave else { return }
        let result = addAccount.invoke(
            type: state.type,
            name: state.name,
            currency: state.currency,
            amount: state.amount,
            details: state.details.draft
        )
        if let invalid = result as? AddAccountResultInvalid {
            state.left.formUnion(invalid.invalid)
        } else if result is AddAccountResultCreated {
            state.saved = true
        }
    }
}
