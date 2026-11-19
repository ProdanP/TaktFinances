import SharedLogic
import SwiftUI

struct HomeView: View {
    var onAddAccount: () -> Void = {}
    var onOpenAccount: (_ id: Int64, _ checkIn: Bool) -> Void = { _, _ in }

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var model: HomeModel
    /// The account shown beside My Finances on regular width; nil shows the placeholder.
    @State private var opened: OpenedAccount?
    private let repository: AccountRepository

    init(
        repository: AccountRepository,
        onAddAccount: @escaping () -> Void = {},
        onOpenAccount: @escaping (_ id: Int64, _ checkIn: Bool) -> Void = { _, _ in }
    ) {
        self.repository = repository
        self.onAddAccount = onAddAccount
        self.onOpenAccount = onOpenAccount
        _model = State(initialValue: HomeModel(repository: repository))
    }

    var body: some View {
        let palette = FinancesPalette.scheme(
            colorScheme: colorScheme,
            contrast: colorSchemeContrast
        )
        Group {
            if horizontalSizeClass == .compact {
                compactTabs(palette: palette)
            } else {
                regularSplit(palette: palette)
            }
        }
        .background(palette.background)
        .toolbar { addToolbar(for: model.state.selected) }
        .navigationTitle(model.state.selected == .myfinances ? model.state.selected.title : "")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(model.state.selected == .myfinances ? .visible : .hidden, for: .navigationBar)
        .onChange(of: horizontalSizeClass) { _, sizeClass in
            // Collapsing to one column leaves the opened account on its own, pushed over My Finances.
            if sizeClass == .compact, let opened {
                self.opened = nil
                onOpenAccount(opened.id, false)
            }
        }
    }

    @ViewBuilder
    private func compactTabs(palette: FinancesColorScheme) -> some View {
        TabView(selection: tabSelection) {
            ForEach(model.state.destinations, id: \.name) { destination in
                destinationBody(destination, palette: palette)
                    .tabItem {
                        Label(destination.title, systemImage: destination.systemImage)
                    }
                    .tag(destination)
            }
        }
    }

    @ViewBuilder
    private func regularSplit(palette: FinancesColorScheme) -> some View {
        if model.state.selected == .myfinances {
            // My Finances is the list column and Account Detail the detail column beside it.
            NavigationSplitView {
                sidebar
            } content: {
                destinationBody(.myfinances, palette: palette)
            } detail: {
                if let opened {
                    AccountDetailView(
                        repository: repository,
                        accountId: opened.id,
                        checkInOnOpen: opened.checkIn,
                        onChanged: { model.send(.shown) },
                        onDeleted: { neighbourId in
                            model.send(.shown)
                            // The detail column moves to the neighbour, or to the placeholder when none is left.
                            self.opened = neighbourId.map { OpenedAccount(id: $0, checkIn: false, token: UUID()) }
                        }
                    )
                        .id(opened)
                } else {
                    accountPlaceholder(palette: palette)
                }
            }
        } else {
            NavigationSplitView {
                sidebar
            } detail: {
                destinationBody(model.state.selected, palette: palette)
            }
        }
    }

    private var sidebar: some View {
        List(selection: sidebarSelection) {
            ForEach(model.state.destinations, id: \.name) { destination in
                Label(destination.title, systemImage: destination.systemImage)
                    .tag(Optional(destination))
            }
        }
        .navigationTitle("Home")
    }

    private func accountPlaceholder(palette: FinancesColorScheme) -> some View {
        VStack(spacing: 8) {
            Image(systemName: "building.columns.fill").foregroundStyle(palette.onSurfaceVariant)
            Text("Open an account to see its history")
                .font(.body)
                .foregroundStyle(palette.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(palette.background)
    }

    /// Beside My Finances on regular width; pushed over it on compact width.
    private func openAccount(_ id: Int64, _ checkIn: Bool) {
        if horizontalSizeClass == .compact {
            onOpenAccount(id, checkIn)
        } else {
            opened = OpenedAccount(id: id, checkIn: checkIn, token: UUID())
        }
    }

    @ToolbarContentBuilder
    private func addToolbar(for destination: HomeDestination) -> some ToolbarContent {
        if destination == .myfinances {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: onAddAccount) {
                    Image(systemName: "plus")
                }
                .accessibilityLabel("Add account")
            }
        }
    }

    private var tabSelection: Binding<HomeDestination> {
        Binding(
            get: { model.state.selected },
            set: { model.send(.destinationSelected($0)) }
        )
    }

    private var sidebarSelection: Binding<HomeDestination?> {
        Binding(
            get: { model.state.selected },
            set: { destination in
                if let destination {
                    model.send(.destinationSelected(destination))
                }
            }
        )
    }

    @ViewBuilder
    private func destinationBody(_ destination: HomeDestination, palette: FinancesColorScheme) -> some View {
        if destination == .myfinances {
            MyFinancesView(
                content: model.state.finances,
                balances: model.state.balances,
                palette: palette,
                send: model.send,
                onAddFirstAccount: onAddAccount,
                onOpenAccount: openAccount
            )
        } else {
            destinationMock(title: destination.title, palette: palette)
        }
    }

    private func destinationMock(title: String, palette: FinancesColorScheme) -> some View {
        Text(title)
            .font(.title)
            .foregroundStyle(palette.onBackground)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(palette.background)
    }
}

/// An account opened beside My Finances. `token` makes "Check in" on the same account reopen it with the sheet.
private struct OpenedAccount: Hashable {
    let id: Int64
    let checkIn: Bool
    let token: UUID
}

private extension HomeDestination {
    var systemImage: String {
        switch self {
        case .dashboard:
            return "house.fill"
        case .myfinances:
            return "building.columns.fill"
        case .profile:
            return "person.fill"
        default:
            return "circle"
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        HomeView(repository: PreviewAccountRepository())
    }
}
