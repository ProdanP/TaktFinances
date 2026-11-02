# Build

## Notes

Added `:feature:home` with `HomeDestination` and `homeDestinations()`. `:sharedLogic` depends on it with `api` and exports it from `SharedLogic`. `:sharedUI` depends on it, Navigation Compose, and Material 3 window size class (listed in `docs/system/stack.md`). `LandingViewModel` emits `OpenHome` on Continue. `App` hosts a NavHost from Landing to Home. `HomeScreen` maps `WindowWidthSizeClass` from `WindowSizeClass.calculateFromSize` to NavigationBar / NavigationRail / permanent drawer, with empty title mocks and a 420-wide content bound. iOS `ContentView` switches to `HomeView` on Continue; compact uses `TabView`, regular uses `NavigationSplitView`. No Figma frame for this bolt.
