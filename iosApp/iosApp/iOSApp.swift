import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    init() {
        TaktIosKt.startTaktOnIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}