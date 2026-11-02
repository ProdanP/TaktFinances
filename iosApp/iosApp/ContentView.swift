import SwiftUI

struct ContentView: View {
    @State private var showHome = false

    var body: some View {
        if showHome {
            HomeView()
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
