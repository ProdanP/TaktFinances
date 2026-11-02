# Design

## Approach

`:feature:home` is a new Kotlin Multiplatform module with the same targets as `:sharedLogic`. Register it in `settings.gradle.kts`. It has no repository and no use case. The public domain value is the three destinations both UIs show:

```kotlin
enum class HomeDestination(val title: String) {
    Dashboard("Dashboard"),
    MyFinances("My Finances"),
    Profile("Profile"),
}

fun homeDestinations(): List<HomeDestination> = HomeDestination.entries
```

`:sharedLogic` depends on the module with `api`. `:sharedUI` depends on the module and uses those types only.

The shell owns Landing → Home navigation. Add Navigation Compose (`org.jetbrains.androidx.navigation:navigation-compose`) to the version catalog and to `:sharedUI`, as already allowed in `docs/system/stack.md`. Add Material 3 window size class (`androidx.compose.material3:material3-window-size-class` / the Compose Multiplatform artifact that exports it) to `docs/system/stack.md` and the catalog, then to `:sharedUI`. Detect form factor with the framework types `WindowWidthSizeClass.Compact`, `WindowWidthSizeClass.Medium`, and `WindowWidthSizeClass.Expanded` from the current window size (`calculateWindowSizeClass` on Android, or `WindowSizeClass.calculateFromSize` with the window size on desktop). Do not invent local dp cutoffs in Home. Map those classes to Material 3 chrome in the view only: Compact → `NavigationBar`, Medium → `NavigationRail`, Expanded → permanent navigation drawer. Do not add `NavigationSuiteScaffold`. The ViewModel does not branch on form factor.

`App` hosts a small NavHost with routes `landing` and `home`, starting on `landing`. Android and desktop share one MVI `LandingViewModel`. `ContinueTapped` emits a one-shot `OpenHome` effect. The shell collects that effect and navigates to `home`. Nothing is stored. `HomeScreen` is the start destination content after that navigation.

Android and desktop share one MVI `HomeViewModel` in `:sharedUI` at `com.prodan.taktfinances.home`. `HomeState` holds the selected `HomeDestination` (default `Dashboard`) and the destination list from `homeDestinations()`. The only intent is `DestinationSelected`. There is no effect channel. `HomeScreen` collects state and paints theme `background`. Compact width shows a bottom `NavigationBar`. Medium width shows a leading `NavigationRail`. Expanded width shows a permanent navigation drawer. The selected destination body is an empty mock: centered destination title on `background`, with content width bounded at 420 dp when the pane is wider. Tab icons use the Material icons already available through Material 3 (home, account balance, person) or equivalent vector resources already in the project if icons are not yet a dependency—prefer Material Icons Extended only if already on the classpath; otherwise use labeled destinations without new icon libraries.

iOS uses `@Observable` `HomeModel` and `HomeView` in `iosApp/iosApp/Home/`. The model holds the selected destination. `ContentView` owns a `NavigationStack` (or equivalent root switch): `LandingView` first; on Continue it pushes or replaces with `HomeView`. Detect size with SwiftUI’s native `horizontalSizeClass` (`UserInterfaceSizeClass`). `.compact` uses `TabView` (bottom tabs). `.regular` uses `NavigationSplitView` with a sidebar (iOS has no separate Medium/Expanded classes). The model does not read size class. Each tab body is an empty mock showing the destination title. Continue still does not write onboarding state.

A `commonTest` in `:feature:home` asserts that `homeDestinations()` returns Dashboard, My Finances, and Profile with those titles. A Compose or ViewModel test is not required beyond that domain check.

## Non-goals

- Real Dashboard, My Finances, or Profile content beyond empty title mocks
- Separate feature modules for the three tabs
- Persisting the selected tab or that Continue was tapped
- `NavigationSuiteScaffold` or other Material Adaptive navigation-suite APIs
- Hand-rolled width cutoffs instead of `WindowWidthSizeClass` / `horizontalSizeClass`
- Changing the theme, typography, or shapes
- Drafting or matching Figma frames in this bolt

## Acceptance criteria

- Tapping Continue on Landing opens Home on Android, desktop, and iOS.
- Home offers Dashboard, My Finances, and Profile; each body is an empty mock showing that destination’s title.
- Compose chooses chrome from `WindowWidthSizeClass`: Compact → bottom bar, Medium → NavigationRail, Expanded → permanent drawer.
- iOS chooses chrome from `horizontalSizeClass`: compact → `TabView`, regular → `NavigationSplitView` sidebar.
- Home domain lives in `:feature:home`; Compose in `:sharedUI` at `com.prodan.taktfinances.home`; SwiftUI in `iosApp/iosApp/Home/`.
- The shell owns the Landing → Home route; features do not navigate into each other.

## Figma

none
