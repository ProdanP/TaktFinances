import SharedLogic
import SwiftUI

struct AddAccountView: View {
    @Environment(\.colorScheme) private var colorScheme
    @State private var model = AddAccountModel()
    private let onFinished: () -> Void

    init(onFinished: @escaping () -> Void) {
        self.onFinished = onFinished
    }

    var body: some View {
        let palette = FinancesPalette.scheme(colorScheme: colorScheme)
        Form {
            Picker("Type", selection: typeBinding) {
                Text("Select").tag(Optional<AccountType>.none)
                Text("Bank").tag(Optional(AccountType.bank))
                Text("Investment").tag(Optional(AccountType.investment))
            }
            if model.state.type != nil {
                TextField("Name", text: nameBinding)
                Picker("Currency", selection: currencyBinding) {
                    Text("Select").tag(Optional<Currency>.none)
                    Text("Euro").tag(Optional(Currency.eur))
                    Text("US dollar").tag(Optional(Currency.usd))
                    Text("Moldovan leu").tag(Optional(Currency.mdl))
                }
                TextField("Current amount", text: amountBinding)
                    .keyboardType(.decimalPad)
                if let currency = model.state.currency, let preview = previewAmount(currency: currency) {
                    Text(preview)
                        .foregroundStyle(palette.onSurfaceVariant)
                }
                if model.state.unavailable {
                    Text("Could not save the account.")
                        .foregroundStyle(palette.error)
                }
                Button("Save") {
                    Task {
                        await model.send(.saveTapped)
                        if model.state.didSave {
                            onFinished()
                        }
                    }
                }
            }
        }
        .navigationTitle("Add Account")
        .background(palette.background)
    }

    private var typeBinding: Binding<AccountType?> {
        Binding(
            get: { model.state.type },
            set: { value in
                if let value {
                    Task { await model.send(.typeSelected(value)) }
                }
            }
        )
    }

    private var nameBinding: Binding<String> {
        Binding(
            get: { model.state.name },
            set: { value in Task { await model.send(.nameChanged(value)) } }
        )
    }

    private var currencyBinding: Binding<Currency?> {
        Binding(
            get: { model.state.currency },
            set: { value in
                if let value {
                    Task { await model.send(.currencySelected(value)) }
                }
            }
        )
    }

    private var amountBinding: Binding<String> {
        Binding(
            get: { model.state.amountText },
            set: { value in Task { await model.send(.amountChanged(value)) } }
        )
    }

    private func previewAmount(currency: Currency) -> String? {
        let canonical = model.state.amountText
            .replacingOccurrences(of: ",", with: ".")
        guard let units = MinorUnitsKt.minorUnits(canonical: canonical)?.int64Value else {
            return nil
        }
        return formatMoney(Money(minorUnits: units, currency: currency))
    }
}
