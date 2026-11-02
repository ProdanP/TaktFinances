# Tasks

## Tasks

test: ./gradlew :feature:home:jvmTest

- [x] Add Material 3 window size class and Navigation Compose to `docs/system/stack.md` and `gradle/libs.versions.toml`, then depend on both from `:sharedUI`.
- [x] Add `:feature:home` with the same targets as `:sharedLogic`, register it in `settings.gradle.kts`, and expose `HomeDestination` plus `homeDestinations()` with Dashboard, My Finances, and Profile. No repository and no use case.
- [x] Depend on the module from `:sharedLogic` with `api` and from `:sharedUI` directly. Add a `commonTest` that asserts `homeDestinations()` returns those three titles in order.
- [x] Emit `OpenHome` from `LandingViewModel` on `ContinueTapped`. Host `landing` and `home` in a shell NavHost in `App`, starting on Landing, and navigate to Home when the effect is collected.
- [x] Add `HomeViewModel` and `HomeScreen` in `:sharedUI` at `com.prodan.taktfinances.home`. State holds the selected destination from `homeDestinations()`. `DestinationSelected` updates selection. Empty mock body shows the destination title, content width bounded at 420 dp when the pane is wider.
- [x] Map `WindowWidthSizeClass` in the Home view: Compact → `NavigationBar`, Medium → `NavigationRail`, Expanded → permanent navigation drawer. Do not use local dp cutoffs or `NavigationSuiteScaffold`.
- [x] Add `HomeModel` and `HomeView` in `iosApp/iosApp/Home/`. Wire Continue from Landing so `ContentView` shows Home. Compact `horizontalSizeClass` uses `TabView`; regular uses `NavigationSplitView` with a sidebar. Each destination body is an empty title mock.
