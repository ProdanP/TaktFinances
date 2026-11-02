# Verify

## Result

`:feature:home` exposes `homeDestinations()` as Dashboard, My Finances, Profile. `./gradlew :feature:home:jvmTest` passed that assertion.

`LandingViewModel` emits `OpenHome` on Continue. `App` NavHost starts on Landing and navigates to `HomeScreen`. Home maps `WindowWidthSizeClass` from `WindowSizeClass.calculateFromSize` to NavigationBar, NavigationRail, or permanent drawer. Tab bodies are empty title mocks with a 420 content bound. Destinations use Material icons (Home, AccountBalance, Person).

iOS `ContentView` opens `HomeView` on Continue. Compact uses `TabView` with SF Symbol labels; regular uses `NavigationSplitView` with a sidebar. Domain lives in `:feature:home`; Compose in `com.prodan.taktfinances.home`; SwiftUI in `iosApp/iosApp/Home/`. Shell owns the Landing → Home route.

## Test

command: ./gradlew :feature:home:jvmTest
exit_code: 0

## Acceptance

- [x] Tapping Continue on Landing opens Home on Android, desktop, and iOS.
- [x] Home offers Dashboard, My Finances, and Profile; each body is an empty mock showing that destination’s title.
- [x] Compose chooses chrome from `WindowWidthSizeClass`: Compact → bottom bar, Medium → NavigationRail, Expanded → permanent drawer.
- [x] iOS chooses chrome from `horizontalSizeClass`: compact → `TabView`, regular → `NavigationSplitView` sidebar.
- [x] Home domain lives in `:feature:home`; Compose in `:sharedUI` at `com.prodan.taktfinances.home`; SwiftUI in `iosApp/iosApp/Home/`.
- [x] The shell owns the Landing → Home route; features do not navigate into each other.
