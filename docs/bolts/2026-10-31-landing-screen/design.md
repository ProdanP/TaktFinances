# Design

## Approach

`:feature:onboarding` is a new Kotlin Multiplatform module with the same targets as `:sharedLogic`. Register it in `settings.gradle.kts`. It has no repository and no use case. Continue has no rule, and nothing is stored. The public domain value is the welcome both screens show:

```kotlin
data class LandingWelcome(val title: String, val message: String)

fun landingWelcome(): LandingWelcome = LandingWelcome(
    title = "Welcome",
    message = "Takt Finances keeps a clear view of your money, so the next step is yours.",
)
```

`:sharedLogic` depends on the module with `api`, so the `SharedLogic` framework exports `landingWelcome()`. `:sharedUI` depends on the module and uses that function only.

Android and desktop share one MVI `LandingViewModel` in `:sharedUI` at `com.prodan.taktfinances.onboarding`. `LandingState` holds the title and message, initialized from `landingWelcome()`. The only intent is `ContinueTapped`. `onIntent` does not change state and does not emit an effect, so there is no effect channel. `LandingScreen` collects the state and paints the theme `background` for the whole screen. From top to bottom the column is the title, then the illustration, then the message, then a filled Continue button pinned to the bottom with 24 point insets from the sides and the bottom safe area. The Content area that holds the illustration and message has no fill of its own, so it does not draw a white rectangle over the screen. The illustration asset is drawn on the same `background` color as the screen (`#F9F9FF`), not on pure white. On a window wider than 420, that column stays 420 wide and horizontally centered, and the button spans the column while remaining pinned to the bottom. `App` shows this screen instead of the greeting and keeps the existing `MaterialTheme`. Typography and shapes stay the Material 3 defaults.

The illustration is the Figma node `Landing illustration` (coins and an open notebook) with a `#F9F9FF` field behind the art. Build copies that raster into the Compose resource `landing` and the iOS asset `Landing`. It is 240 by 240 on the screen.

iOS uses one `@Observable` `LandingModel` and `LandingView` in `iosApp/iosApp/Onboarding/`. The model reads the exported `landingWelcome()` into its own state. `send(.continueTapped)` does nothing. `ContentView` shows `LandingView` and paints the existing palette `background` for the whole screen. The layout matches the phone frames: title at the top, illustration, message under the illustration, Continue at the bottom. The middle content has no separate fill. The button is filled with `primary`, uses a 12 point corner radius, and uses the system font. `ContentView` no longer shows the template greeting.

A `commonTest` in `:feature:onboarding` asserts that `landingWelcome()` returns the title and message above.

## Non-goals

- A destination, a route, or a saved record when Continue is tapped
- Another onboarding step
- Generating the welcome or the illustration at runtime
- Changing the screen color away from theme `background`
- A change to the theme, typography, or shapes
- Removing the template `Greeting` types
- A separate dark illustration

## Acceptance criteria

- Android, desktop, and iOS open on Landing instead of the template greeting.
- The screen uses theme `background`. Phone, tablet, and desktop Content areas have no white rectangle behind or around the illustration.
- The title is `Welcome` at the top, and the message is `Takt Finances keeps a clear view of your money, so the next step is yours.` under the illustration. Both come from `landingWelcome()`.
- The illustration is the coins-and-notebook image in the Figma frame below, with the same `#F9F9FF` field as the screen.
- Continue is pinned to the bottom. Tapping it does not navigate and does not change what is stored.
- On a Compose window wider than 420, the column is 420 wide, horizontally centered, and the button still spans that column at the bottom.
- The screen uses the existing Material 3 theme on Android and desktop, and the existing iOS palette and system font.

## Figma

https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=18-41
