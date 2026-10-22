# System

Living description of this project. When a bolt changes behavior, fold a short delta in here and leave the bolt folder as history.

Kotlin Multiplatform app for Android, desktop, and iOS. Shared business rules live in feature modules. Android and desktop UI is Compose in `:sharedUI`. iOS UI is SwiftUI in `iosApp`. Module boundaries, layer rules, and the patterns to use are in [architecture.md](architecture.md). Screen state is in [presentation.md](presentation.md). Repositories, SQL, and HTTP are in [data-access.md](data-access.md). Recoverable failures are in [errors.md](errors.md). Allowed libraries are in [stack.md](stack.md).
