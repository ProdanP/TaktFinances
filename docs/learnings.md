# Learnings

Lessons worth keeping from closed bolts. Add a line only when something surprised you.

`Settings.Secure.CONTRAST_LEVEL` is hidden from the public Android SDK. Observe the setting by the name `contrast_level`, and read the level from `UiModeManager.getContrast()`.
`FinancesPalette.scheme` takes SwiftUI `ColorSchemeContrast` from `\.colorSchemeContrast`. `\.accessibilityContrast` is a different type and does not compile against that parameter.
Compose Material Icons Extended on Maven Central tops out at 1.7.3 and is not published under the Compose Multiplatform 1.12.x version line; pin that artifact explicitly instead of `version.ref = composeMultiplatform`.

