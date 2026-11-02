import Foundation
import SharedLogic

struct LandingState {
    var title: String = ""
    var message: String = ""
}

enum LandingIntent {
    case continueTapped
}

@Observable
final class LandingModel {
    private(set) var state = LandingState()

    init() {
        let welcome = LandingWelcomeKt.landingWelcome()
        state = LandingState(
            title: welcome.title,
            message: welcome.message
        )
    }

    func send(_ intent: LandingIntent) {
        switch intent {
        case .continueTapped:
            break
        }
    }
}
