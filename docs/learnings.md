# Learnings

Lessons worth keeping from finished changes. Add a line only when something surprised you.

`Settings.Secure.CONTRAST_LEVEL` is hidden from the public Android SDK. Observe the setting by the name `contrast_level`, and read the level from `UiModeManager.getContrast()`.
`FinancesPalette.scheme` takes SwiftUI `ColorSchemeContrast` from `\.colorSchemeContrast`. `\.accessibilityContrast` is a different type and does not compile against that parameter.
Compose Material Icons Extended on Maven Central tops out at 1.7.3 and is not published under the Compose Multiplatform 1.12.x version line; pin that artifact explicitly instead of `version.ref = composeMultiplatform`.

Kotlin enum entries import into Swift fully lowercased (`BankAccount` → `.bankaccount`, `MyFinances` → `.myfinances`). Top-level functions in `Currency.kt` are on `CurrencyKt`, one Swift class per Kotlin file.
A SwiftUI `.toolbar` on a child of a `TabView` does not show in a parent `NavigationStack`. Attach the toolbar to the `TabView`'s container and switch its content on the selected tab.
Domain `commonTest` that reads an effect `Flow` needs `kotlinx-coroutines-test`; sending with `Channel.trySend` in the ViewModel keeps those tests free of a Main dispatcher.
The theme uses the Material Color Utilities 2025 color spec (M3 Expressive). `@material/material-color-utilities` 0.4.0 on npm ships extensionless ESM imports, so Node needs a resolve hook that retries with `.js`. Build the scheme with `new DynamicScheme({ variant: TONAL_SPOT, specVersion: "2025", contrastLevel })`; the old Theme Builder export (2021 spec) gives different surface and error tones.
