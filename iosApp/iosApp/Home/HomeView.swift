import SharedLogic
import SwiftUI

struct HomeView: View {
    var onAddAccount: () -> Void = {}

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.colorSchemeContrast) private var colorSchemeContrast
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var model: HomeModel = HomeModel()

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
    }

    @ViewBuilder
    private func compactTabs(palette: FinancesColorScheme) -> some View {
        TabView(selection: tabSelection) {
            ForEach(model.state.destinations, id: \.name) { destination in
                destinationMock(title: destination.title, palette: palette)
                    .tabItem {
                        Label(destination.title, systemImage: destination.systemImage)
                    }
                    .tag(destination)
            }
        }
    }

    @ViewBuilder
    private func regularSplit(palette: FinancesColorScheme) -> some View {
        NavigationSplitView {
            List(selection: sidebarSelection) {
                ForEach(model.state.destinations, id: \.name) { destination in
                    Label(destination.title, systemImage: destination.systemImage)
                        .tag(Optional(destination))
                }
            }
            .navigationTitle("Home")
        } detail: {
            destinationMock(title: model.state.selected.title, palette: palette)
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

    private func destinationMock(title: String, palette: FinancesColorScheme) -> some View {
        Text(title)
            .font(.title)
            .foregroundStyle(palette.onBackground)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(palette.background)
    }
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
        HomeView()
    }
}
