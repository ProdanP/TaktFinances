# Intent

## Problem

The current shell still uses the template defaults: Compose `MaterialTheme` with no custom scheme on Android and desktop, and SwiftUI defaults on iOS. Later screens have no theme to inherit. The color source is the Material Theme Builder export in this bolt, seed `#769CDF`.

The export contains color only. Android and desktop keep Material 3 default typography and shapes. iOS keeps the system font and system materials, and takes its semantic colors from the same export. Desktop follows the system light or dark appearance and uses the standard scheme for that appearance, because a desktop window does not report a contrast preference. Android and iOS select the matching contrast scheme when the system requests higher contrast.

## Done when

- Android and desktop render the current shell inside a Material 3 theme whose colors match the light, dark, and contrast schemes in `docs/bolts/2026-10-31-design-system/material-theme.json`.
- Android selects the scheme from the system light/dark appearance and the system contrast preference.
- Desktop selects the standard light or dark scheme from the system appearance.
- iOS renders the current shell with semantic colors taken from that export, the system font, and system materials, and selects the scheme from the system appearance and contrast preference.
- The greeting behavior of the shell stays as it is, and later screens inherit this theme.
