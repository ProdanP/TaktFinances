# Stack

Versions live in `gradle/libs.versions.toml`. This file is the set of allowed choices. Add a library here before adding it to the version catalog. Add the dependency to a module only when a type in that module uses it.

`domain` stays on the Kotlin standard library. SQLDelight, Ktor, and Koin are `data` and composition-root tools.

## In use

- Kotlin Multiplatform for Android, iOS (`iosArm64`, `iosSimulatorArm64`), and desktop (JVM 11).
- Compose Multiplatform and Material 3 for Android and desktop UI.
- Compose Material Icons Extended (`org.jetbrains.compose.material:material-icons-extended`, 1.7.3) for navigation and screen icons on Android and desktop.
- Material 3 window size class (`org.jetbrains.compose.material3:material3-window-size-class`) for `WindowWidthSizeClass` on Android and desktop.
- SwiftUI for iOS UI. The iOS app imports the `SharedLogic` framework.
- AndroidX Activity Compose for the Android entry. AndroidX Lifecycle ViewModel and Runtime Compose for Compose state holders.
- kotlinx.coroutines, including the Swing dispatcher on desktop.
- kotlinx-coroutines-test for coroutine and Flow tests in `commonTest`.
- kotlin-test, JUnit, and AndroidX test (Espresso) for tests.

## Persistence — SQLDelight

`app.cash.sqldelight`. One SQLite database for the app. Each feature owns the `.sq` files for its tables.

Drivers are platform ports, created in the shells and passed into `:sharedLogic`: the Android driver, the native driver on iOS, and the JDBC driver on desktop. A repository maps generated query types to domain models. Query types stay in `data`. The boundary is [data-access.md](data-access.md).

Room is not used. Room 3 is still alpha, and a ledger keeps SQL as the source of the schema.

## Networking — Ktor client

`io.ktor:ktor-client-core` with ContentNegotiation. JSON is `kotlinx-serialization-json`.

Engines: OkHttp on Android, Darwin on iOS, CIO on desktop. Request and response types stay in `data`. The boundary is [data-access.md](data-access.md).

## Dependency injection — Koin

`io.insert-koin:koin-core`, and only in `:sharedLogic`, where the graph is built.

Screens, use cases, repositories, and SwiftUI take constructor arguments. Do not call `get`, `inject`, or `koinInject` from a feature, a composable, or a Swift view.

Metro and kotlin-inject are not used. Their compile-time contribution across multi-module native targets is still limited, and this app's features are separate modules compiled for iOS.

## Navigation

Android and desktop use Navigation Compose, `org.jetbrains.androidx.navigation:navigation-compose`, in `:sharedUI`. iOS uses SwiftUI `NavigationStack` in `iosApp`.

The shell owns the routes. A feature does not navigate into another feature. Voyager and Decompose are not used: iOS screens stay SwiftUI, so there is no shared navigator.
