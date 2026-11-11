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
}
