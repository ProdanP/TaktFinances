import SharedLogic
import SwiftUI

private enum Route: Hashable {
    case addAccount
}

struct ContentView: View {
    @State private var showHome = false
    @State private var path: [Route] = []
    @State private var accounts: AccountRepository = AccountsFactoryKt.createAccountRepository()

    var body: some View {
        if showHome {
            NavigationStack(path: $path) {
                HomeView(onAddAccount: { path.append(.addAccount) })
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case .addAccount:
                            AddAccountView(repository: accounts, onClose: { path.removeAll() })
                        }
                    }
            }
        } else {
            LandingView(onContinue: { showHome = true })
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
