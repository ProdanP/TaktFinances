import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // doInitKoin: Kotlin/Native renames methods with the init prefix
        // because of the ObjC initializer family
        KoinInitializer.shared.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
