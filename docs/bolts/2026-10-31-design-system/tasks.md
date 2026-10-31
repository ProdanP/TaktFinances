# Tasks

## Tasks

test: ./gradlew :sharedUI:jvmTest

- [x] Add the six Material 3 `ColorScheme` values in `:sharedUI` `commonMain`, copied from `docs/bolts/2026-10-31-design-system/material-theme.json`.
- [x] Add the dark-and-contrast selector, and a `commonTest` for light standard primary `#415F91`, dark standard primary `#AAC7FF`, light high primary `#032B5B`, and dark medium primary `#CDDDFF`.
- [x] Wrap `App` in `MaterialTheme` with that scheme and the default Material 3 type and shape. The shell still paints `primaryContainer` and still uses a Material 3 button.
- [x] In `androidApp`, read `UiModeManager.getContrast()` on API 34 and above, pass standard below that, and map below 0.34 to standard, below 0.67 to medium, and 0.67 and above to high. Update `App` when contrast changes.
- [x] In `desktopApp`, pass standard contrast. Light and dark follow the system appearance.
- [x] In `iosApp`, add the palette, set the page background and control tint from it, and set `AccentColor` to the light and dark primary. Increased contrast uses the high scheme. The button stays a text button and the font stays the system font.
- [x] Match the Android · Light shell frame: `primaryContainer` background, primary button, onPrimary label.
- [x] Match the Android · Dark shell frame with the dark scheme.
- [x] Match the Desktop · Light shell frame with the standard light scheme.
- [x] Match the Desktop · Dark shell frame with the standard dark scheme.
- [x] Match the iOS · Light shell frame: export background, primary text button, system font.
- [x] Match the iOS · Dark shell frame with the dark scheme.
- [x] Leave the Figma card, text field, and button mocks as illustrations. The app shell is the only screen this bolt changes.
