# Tasks

## Tasks

test: ./gradlew :feature:onboarding:jvmTest

- [x] Add `:feature:onboarding` with the same targets as `:sharedLogic`, register it in `settings.gradle.kts`, and expose `LandingWelcome` plus `landingWelcome()` with the title and message from the design. No repository and no use case.
- [x] Depend on the module from `:sharedLogic` with `api` and from `:sharedUI` directly. Add a `commonTest` that asserts `landingWelcome()` returns that title and message.
- [x] Copy the Figma node `Landing illustration` into the Compose resource `landing` and the iOS asset `Landing`. The asset uses the same `#F9F9FF` field as the screen, not a white rectangle.
- [x] Add `LandingViewModel` and `LandingScreen` in `:sharedUI` at `com.prodan.taktfinances.onboarding`. State comes from `landingWelcome()`. `ContinueTapped` changes nothing and emits no effect. Show the screen from `App` inside the existing `MaterialTheme`, in place of the greeting.
- [x] Match the Android / phone frame: theme `background`, Welcome at the top, 240 illustration with no white Content rectangle, message under the illustration, and a filled Continue button pinned to the bottom with 24 point insets.
- [x] Match the Desktop / wide frame: the same Compose screen, with a 420-wide column centered on a wider window and the button spanning that column at the bottom. No white Content rectangle.
- [x] Add `LandingModel` and `LandingView` in `iosApp/iosApp/Onboarding/`. The model reads the exported `landingWelcome()`. `continueTapped` does nothing. `ContentView` shows `LandingView` and paints theme `background` for the whole screen.
- [x] Match the iOS frame: Welcome at the top, the `Landing` asset with no white Content rectangle, message under the illustration, a filled primary button with a 12 point corner radius, and the system font.
