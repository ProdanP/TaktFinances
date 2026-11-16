import SharedLogic
import SwiftUI

private enum Route: Hashable {
    case addAccount
}

struct ContentView: View {
    @State private var showHome = false
    @State private var path: [Route] = []
    /// Built by the composition root in SharedLogic; passed to screens through their initialisers.
    private let dependencies = AppDependencies()

    var body: some View {
        if showHome {
            NavigationStack(path: $path) {
                HomeView(repository: dependencies.accountRepository, onAddAccount: { path.append(.addAccount) })
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case .addAccount:
                            AddAccountView(repository: dependencies.accountRepository, onClose: { path.removeAll() })
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
