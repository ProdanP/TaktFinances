import SharedLogic
import SwiftUI

struct AddAccountView: View {
    var onClose: () -> Void = {}

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @State private var model: AddAccountModel

    init(repository: AccountRepository, onClose: @escaping () -> Void = {}) {
        self.onClose = onClose
        _model = State(initialValue: AddAccountModel(repository: repository))
    }

    var body: some View {
        let palette = FinancesPalette.scheme(
            colorScheme: colorScheme,
            contrast: colorSchemeContrast
        )
        Form {
            Section {
                Picker("Type", selection: typeBinding) {
                    ForEach(model.state.types, id: \.name) { type in
                        Text(type.title).tag(type)
                    }
                }
                .pickerStyle(.segmented)
            }
            Section {
                TextField("Name", text: nameBinding)
                if model.state.nameInvalid {
                    Text("Enter a name").font(.footnote).foregroundStyle(.red)
                }
                Picker("Currency", selection: currencyBinding) {
                    ForEach(model.state.currencies, id: \.name) { currency in
                        Text(currency.code).tag(currency)
                    }
                }
                TextField("Amount", text: amountBinding)
                    .keyboardType(.decimalPad)
                if model.state.amountInvalid {
                    Text(amountError).font(.footnote).foregroundStyle(.red)
                }
            }
            detailsSection
            Section {
                Button("Save") { model.send(.saveTapped) }
                    .frame(maxWidth: .infinity)
            }
        }
        .scrollContentBackground(.hidden)
        .background(palette.background)
        .frame(maxWidth: 520)
        .frame(maxWidth: .infinity)
        .background(palette.background)
        .navigationTitle("Add Account")
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: model.state.saved) { _, saved in
            if saved { onClose() }
        }
    }

    @ViewBuilder
    private var detailsSection: some View {
        let errors = model.state.detailErrors
        switch model.state.type {
        case .bankaccount:
            Section {
                Picker(selection: bankBinding) {
                    Text("Choose a bank").tag(Bank?.none)
                    ForEach(model.state.banks, id: \.name) { bank in
                        Label { Text(bank.title) } icon: { BankBadge(bank: bank) }
                            .tag(Bank?.some(bank))
                    }
                } label: {
                    Label {
                        Text("Bank")
                    } icon: {
                        if let bank = model.state.details.bank { BankBadge(bank: bank) }
                    }
                }
                .pickerStyle(.navigationLink)
                fieldError("Choose a bank", show: errors.bank)
                Picker("Account kind", selection: bankKindBinding) {
                    Text("Choose a kind").tag(BankAccountKind?.none)
                    ForEach(model.state.bankAccountKinds, id: \.name) { kind in
                        Text(kind.title).tag(BankAccountKind?.some(kind))
                    }
                }
                fieldError("Choose an account kind", show: errors.accountKind)
                TextField("Last 4 digits (optional)", text: last4Binding)
                    .keyboardType(.numberPad)
                fieldError("Enter exactly 4 digits", show: errors.last4)
            }
        case .investment:
            Section {
                Picker("Investment kind", selection: investmentKindBinding) {
                    Text("Choose a kind").tag(InvestmentKind?.none)
                    ForEach(model.state.investmentKinds, id: \.name) { kind in
                        Text(kind.title).tag(InvestmentKind?.some(kind))
                    }
                }
                fieldError("Choose an investment kind", show: errors.investmentKind)
                TextField("Platform", text: platformBinding)
                fieldError("Enter a platform", show: errors.platform)
            }
        case .loan:
            Section {
                Picker("Direction", selection: directionBinding) {
                    ForEach(model.state.loanDirections, id: \.name) { direction in
                        Text(direction.title).tag(LoanDirection?.some(direction))
                    }
                }
                .pickerStyle(.segmented)
                fieldError("Choose Borrowed or Lent", show: errors.direction)
                TextField("Counterparty", text: counterpartyBinding)
                fieldError("Enter who the loan is with", show: errors.counterparty)
            }
        default:
            EmptyView()
        }
    }

    @ViewBuilder
    private func fieldError(_ text: String, show: Bool) -> some View {
        if show {
            Text(text).font(.footnote).foregroundStyle(.red)
        }
    }

    private var amountError: String {
        let digits = model.state.currency.minorUnitDigits
        return digits == 0 ? "Enter a whole number" : "Enter a number, up to \(digits) decimals"
    }

    private var typeBinding: Binding<AccountType> {
        Binding(get: { model.state.type }, set: { model.send(.typeSelected($0)) })
    }

    private var currencyBinding: Binding<Currency> {
        Binding(get: { model.state.currency }, set: { model.send(.currencySelected($0)) })
    }

    private var nameBinding: Binding<String> {
        Binding(get: { model.state.name }, set: { model.send(.nameChanged($0)) })
    }

    private var amountBinding: Binding<String> {
        Binding(get: { model.state.amount }, set: { model.send(.amountChanged($0)) })
    }

    private var bankBinding: Binding<Bank?> {
        Binding(get: { model.state.details.bank }, set: { if let v = $0 { model.send(.bankSelected(v)) } })
    }

    private var bankKindBinding: Binding<BankAccountKind?> {
        Binding(get: { model.state.details.bankAccountKind }, set: { if let v = $0 { model.send(.bankAccountKindSelected(v)) } })
    }

    private var last4Binding: Binding<String> {
        Binding(get: { model.state.details.last4 }, set: { model.send(.last4Changed($0)) })
    }

    private var investmentKindBinding: Binding<InvestmentKind?> {
        Binding(get: { model.state.details.investmentKind }, set: { if let v = $0 { model.send(.investmentKindSelected(v)) } })
    }

    private var platformBinding: Binding<String> {
        Binding(get: { model.state.details.platform }, set: { model.send(.platformChanged($0)) })
    }

    private var directionBinding: Binding<LoanDirection?> {
        Binding(get: { model.state.details.loanDirection }, set: { if let v = $0 { model.send(.loanDirectionSelected(v)) } })
    }

    private var counterpartyBinding: Binding<String> {
        Binding(get: { model.state.details.counterparty }, set: { model.send(.counterpartyChanged($0)) })
    }
}

/// Monogram badge; replace the body with a logo image when real assets are added.
struct BankBadge: View {
    let bank: Bank
    var size: CGFloat = 24

    var body: some View {
        let argb = UInt32(truncatingIfNeeded: bank.colorArgb)
        Text(bank.initials)
            .font(.system(size: size * 0.4, weight: .bold))
            .foregroundStyle(.white)
            .frame(width: size, height: size)
            .background(
                Circle().fill(
                    Color(
                        red: Double((argb >> 16) & 0xFF) / 255,
                        green: Double((argb >> 8) & 0xFF) / 255,
                        blue: Double(argb & 0xFF) / 255
                    )
                )
            )
    }
}
