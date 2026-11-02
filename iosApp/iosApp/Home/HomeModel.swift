import Foundation
import SharedLogic

struct HomeState {
    var destinations: [HomeDestination] = []
    var selected: HomeDestination = .dashboard
}

enum HomeIntent {
    case destinationSelected(HomeDestination)
}

@Observable
final class HomeModel {
    private(set) var state = HomeState()

    init() {
        let destinations = HomeDestinationKt.homeDestinations()
        state = HomeState(
            destinations: destinations,
            selected: destinations.first ?? .dashboard
        )
    }

    func send(_ intent: HomeIntent) {
        switch intent {
        case let .destinationSelected(destination):
            state.selected = destination
        }
    }
}
