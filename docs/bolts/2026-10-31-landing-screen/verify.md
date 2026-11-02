# Verify

## Result

`:feature:onboarding` exposes `landingWelcome()` with title `Welcome` and message `Takt Finances keeps a clear view of your money, so the next step is yours.` `./gradlew :feature:onboarding:jvmTest` passed that assertion.

`App` shows `LandingScreen` inside the existing `MaterialTheme`. The screen paints `background`, puts Welcome at the top, the 240 `landing` illustration and the message in the middle with no Content fill, and a filled Continue at the bottom with 24 point insets. On a window wider than 420 the column is capped at 420. `ContinueTapped` does not change state and does not emit an effect.

`ContentView` shows `LandingView`. The model reads `LandingWelcomeKt.landingWelcome()`. The layout matches: Welcome, illustration, message, Continue with a 12 point corner radius on `primary`, system font, and palette `background`. `continueTapped` does nothing. Compose and iOS both use the `landing` / `Landing` asset.

## Test

command: ./gradlew :feature:onboarding:jvmTest
exit_code: 0

## Acceptance

- [x] Android, desktop, and iOS open on Landing instead of the template greeting.
- [x] The screen uses theme `background`. Phone, tablet, and desktop Content areas have no white rectangle behind or around the illustration.
- [x] The title is `Welcome` at the top, and the message is `Takt Finances keeps a clear view of your money, so the next step is yours.` under the illustration. Both come from `landingWelcome()`.
- [x] The illustration is the coins-and-notebook image in the Figma frame below, with the same `#F9F9FF` field as the screen.
- [x] Continue is pinned to the bottom. Tapping it does not navigate and does not change what is stored.
- [x] On a Compose window wider than 420, the column is 420 wide, horizontally centered, and the button still spans that column at the bottom.
- [x] The screen uses the existing Material 3 theme on Android and desktop, and the existing iOS palette and system font.
- [x] The implemented screens match https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=18-41
