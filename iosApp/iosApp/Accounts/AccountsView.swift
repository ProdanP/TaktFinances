import SharedLogic
import SwiftUI

struct AccountsView: View {
    @Environment(\.colorScheme) private var colorScheme
    @State private var model = AccountsModel()

    var body: some View {
        let palette = FinancesPalette.scheme(colorScheme: colorScheme)
        NavigationStack {
            List {
                ForEach(model.state.accounts, id: \.id.value) { account in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(account.name)
                                .foregroundStyle(palette.onBackground)
                            Text(account.type.name)
                                .font(.subheadline)
                                .foregroundStyle(palette.onSurfaceVariant)
                        }
                        Spacer()
                        Text(formatMoney(account.balance))
                            .foregroundStyle(palette.onBackground)
                    }
                }
            }
            .navigationTitle("My Finances")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        Task { await model.send(.addTapped) }
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .navigationDestination(isPresented: addBinding) {
                AddAccountView {
                    Task { await model.send(.dismissAdd) }
                }
            }
        }
        .background(palette.background)
        .task {
            await model.send(.opened)
        }
    }

    private var addBinding: Binding<Bool> {
        Binding(
            get: { model.state.showAddAccount },
            set: { presented in
                if presented {
                    Task { await model.send(.addTapped) }
                } else {
                    Task { await model.send(.dismissAdd) }
                }
            }
        )
    }
}

func formatMoney(_ money: Money) -> String {
    let major = Decimal(money.minorUnits) / 100
    let code: String
    switch money.currency {
    case .eur: code = "EUR"
    case .usd: code = "USD"
    case .mdl: code = "MDL"
    default: code = "EUR"
    }
    return major.formatted(
        .currency(code: code)
            .precision(.fractionLength(2))
            .locale(.current)
    )
}
