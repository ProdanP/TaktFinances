# Verify

## Result

Compared `FinancesColorScheme.kt` with `material-theme.json`. All six schemes match the export on every Material 3 role. `shadow` is not a `ColorScheme` parameter, so it is absent. `FinancesPalette.swift` matches the light, dark, light-high, and dark-high schemes on the same roles. `./gradlew :sharedUI:jvmTest` passed, including the four named primaries and the contrast thresholds 0.33, 0.34, 0.66, and 0.67.

`App` wraps the existing greeting column in `MaterialTheme` with `financesColorScheme` and the default type and shape. The column background is `primaryContainer`, and the control is a Material 3 button labeled "Click me!" that still reveals the greeting. Android reads `UiModeManager.getContrast()` from API 34, uses standard below that, and refreshes when `contrast_level` changes. Desktop passes `ThemeContrast.Standard`, and light or dark still comes from `isSystemInDarkTheme()`.

`ContentView` keeps the text button, the system font, and the greeting toggle. The page background is the palette `background` and the tint is `primary`. Increased contrast selects the high scheme. `AccentColor` is light primary `#415F91`, dark primary `#AAC7FF`, light high `#032B5B`, and dark high `#EBF0FF`.

The shell frames in the design-system frame use those same colors. Android and desktop light are background `#D6E3FF`, button `#415F91`, label `#FFFFFF`. Android and desktop dark are background `#284777`, button `#AAC7FF`, label `#0A305F`. iOS light is background `#F9F9FF` and text `#415F91`. iOS dark is background `#111318` and text `#AAC7FF`. Each frame shows "Click me!" at the top. The card, field, and button mocks are not in the app.

## Test

command: ./gradlew :sharedUI:jvmTest
exit_code: 0

## Acceptance

- [x] Android and desktop render the current shell inside a Material 3 theme whose colors match the light, dark, and contrast schemes in `docs/bolts/2026-10-31-design-system/material-theme.json`.
- [x] Android selects the scheme from the system light/dark appearance and the system contrast preference.
- [x] Desktop selects the standard light or dark scheme from the system appearance.
- [x] iOS renders the current shell with semantic colors taken from that export, the system font, and system materials, and selects the scheme from the system appearance and contrast preference.
- [x] The greeting behavior of the shell stays as it is, and later screens inherit this theme.
- [x] The implemented shell screens match https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=6-2171
