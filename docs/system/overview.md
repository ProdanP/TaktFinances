# System

Living description of this project. When a bolt changes behavior, fold a short delta in here and leave the bolt folder as history.

Kotlin Multiplatform app for Android, desktop, and iOS. Shared business rules live in feature modules. Android and desktop UI is Compose in `:sharedUI`. iOS UI is SwiftUI in `iosApp`. Module boundaries, layer rules, and the patterns to use are in [architecture.md](architecture.md). Screen state is in [presentation.md](presentation.md). Repositories, SQL, and HTTP are in [data-access.md](data-access.md). Recoverable failures are in [errors.md](errors.md). Allowed libraries are in [stack.md](stack.md). Screen designs are in the Figma file named in [figma.md](figma.md).

The greeting shell uses the Material Theme Builder export in `docs/bolts/2026-10-31-design-system/material-theme.json`. Android and desktop share the Material 3 color schemes in `:sharedUI`. Android also follows the system contrast preference. Desktop uses the standard scheme for the system appearance. iOS uses the same roles as semantic colors, keeps the system font, and uses the high scheme when contrast is increased.
