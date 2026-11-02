# Learnings

Lessons worth keeping from closed bolts. Add a line only when something surprised you.

`Settings.Secure.CONTRAST_LEVEL` is hidden from the public Android SDK. Observe the setting by the name `contrast_level`, and read the level from `UiModeManager.getContrast()`.
`FinancesPalette.scheme` takes SwiftUI `ColorSchemeContrast` from `\.colorSchemeContrast`. `\.accessibilityContrast` is a different type and does not compile against that parameter.
