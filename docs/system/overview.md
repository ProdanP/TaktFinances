# System

Living description of this project. When a bolt changes behavior, fold a short delta in here and leave the bolt folder as history.

Kotlin Multiplatform app for Android, desktop, and iOS. Shared business rules live in feature modules. Android and desktop UI is Compose in `:sharedUI`. iOS UI is SwiftUI in `iosApp`. Module boundaries, layer rules, and the patterns to use are in [architecture.md](architecture.md). Screen state is in [presentation.md](presentation.md). Phone, tablet, and foldable layouts are in [adaptive-ui.md](adaptive-ui.md). Repositories, SQL, and HTTP are in [data-access.md](data-access.md). Recoverable failures are in [errors.md](errors.md). Allowed libraries are in [stack.md](stack.md). Screen designs are in the Figma file named in [figma.md](figma.md).

The app opens on the onboarding Landing screen. Domain lives in `:feature:onboarding`. Continue opens Home. Home domain lives in `:feature:home` with Dashboard, My Finances, and Profile mocks. Android and desktop share Compose in `:sharedUI` (`com.prodan.taktfinances.onboarding` and `.home`); chrome follows `WindowWidthSizeClass` (bar / rail / permanent drawer). iOS uses SwiftUI in `iosApp/iosApp/Onboarding/` and `Home/`; chrome follows `horizontalSizeClass` (TabView / NavigationSplitView).

Android and desktop share one Material 3 light scheme and one dark scheme in `:sharedUI`, with Inter for Material type. Light and dark follow the system appearance. iOS uses those same light and dark roles as semantic colors and keeps the system font.
