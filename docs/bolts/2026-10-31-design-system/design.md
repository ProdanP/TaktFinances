# Design

## Approach

The six schemes in `docs/bolts/2026-10-31-design-system/material-theme.json` become six Material 3 `ColorScheme` values in `:sharedUI` `commonMain`. The hex values are copied from the export. A selector takes a dark flag and a contrast level of standard, medium, or high and returns one scheme. `App` reads `isSystemInDarkTheme()`, takes a contrast argument that defaults to standard, and wraps the existing shell in `MaterialTheme` with that scheme and the default Material 3 typography and shapes. The shell still paints `primaryContainer` and still uses a Material 3 button.

`androidApp` reads `UiModeManager.getContrast()` on API 34 and above and passes standard on older versions. Below 0.34 is standard, below 0.67 is medium, and 0.67 and above is high. The activity passes the current value into `App` and updates it when contrast changes. `desktopApp` passes standard. Light and dark still follow the system appearance.

iOS keeps the same roles in `iosApp`. `ContentView` reads `colorScheme` and `accessibilityContrast`. Standard contrast uses the standard scheme. Increased contrast uses the high scheme, because iOS exposes those two levels. The page background is the scheme `background`, and controls are tinted with `primary`. The button stays a text button. The system font stays the default. `AccentColor` is set to the light and dark primary.

The Figma file lists every exported role. Android and desktop share one board, with a column for each of the six schemes. iOS has a separate board for light, dark, and the two high-contrast schemes. Under those boards, light and dark mocks show a card, a text field, and a button. Desktop uses the Android mock. The mocks illustrate these colors.

A `commonTest` checks the selector against the export: light standard primary `#415F91`, dark standard primary `#AAC7FF`, light high primary `#032B5B`, and dark medium primary `#CDDDFF`.

## Non-goals

- A component catalog, a new screen, or a change to the greeting interaction
- Custom typography, custom shapes, or wallpaper-derived dynamic color
- Generating schemes from the seed at runtime
- A desktop contrast setting
- An iOS medium-contrast scheme

## Acceptance criteria

- The Compose light, dark, medium-contrast, and high-contrast schemes match `material-theme.json` for primary, onPrimary, primaryContainer, background, onBackground, surface, and error.
- Android on API 34 and above selects the scheme with the contrast mapping in the approach. Older Android versions use the standard scheme. Light and dark follow the system appearance.
- Desktop uses the standard scheme and follows the system appearance.
- The iOS shell uses the export background and primary, the system font, and the standard or high scheme for the system appearance and contrast.
- The shell still shows the greeting button and reveals the greeting on tap.
- Later screens inherit this Compose `MaterialTheme` and this iOS palette.

## Figma

https://www.figma.com/design/xgvzopumAT18TJKevrGIvU/Takt-Finances?node-id=6-2171
