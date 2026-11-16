# Stack

Versions live in `gradle/libs.versions.toml`. This file is the set of allowed choices. Add a library here before adding it to the version catalog. Add the dependency to a module only when a type in that module uses it.

`domain` stays on the Kotlin standard library. Room and Ktor are `data` tools. Koin is a composition-root tool, plus each feature's module file.

## In use

- Kotlin Multiplatform for Android, iOS (`iosArm64`, `iosSimulatorArm64`), and desktop (JVM 11).
- Compose Multiplatform and Material 3 for Android and desktop UI.
- Compose Material Icons Extended (`org.jetbrains.compose.material:material-icons-extended`, 1.7.3) for navigation and screen icons on Android and desktop.
- Material 3 window size class (`org.jetbrains.compose.material3:material3-window-size-class`) for `WindowWidthSizeClass` on Android and desktop.
- SwiftUI for iOS UI. The iOS app imports the `SharedLogic` framework.
- AndroidX Activity Compose for the Android entry. AndroidX Lifecycle ViewModel and Runtime Compose for Compose state holders.
- kotlinx.coroutines, including the Swing dispatcher on desktop. `kotlinx-coroutines-core` is a `data` dependency of a feature that moves blocking work off the caller's thread.
- kotlinx-coroutines-test for coroutine and Flow tests in `commonTest`.
- kotlin-test, JUnit, and AndroidX test (Espresso) for tests.

## Persistence — Room

`androidx.room3` 3.0.x (Gradle plugin, `room3-runtime`, and `room3-compiler` through KSP) with `androidx.sqlite:sqlite-bundled`. One SQLite database for the app, the same bundled SQLite on Android, iOS and desktop.

Each feature owns the `@Entity` and `@Dao` types for its tables, in its `data.local` package, and depends on `room3-runtime` only. `:sharedLogic` declares the one `@Database` (`AppDatabase`, internal), lists every feature's entities, runs `room3-compiler` on `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` and `kspJvm`, and commits the exported schema under `sharedLogic/schemas/`. That schema file is the reviewed record of the database and what an auto-migration diffs against.

The shells start the app through `startTaktOnAndroid(context)`, `startTaktOnDesktop()` and `startTaktOnIos()`, which open the database at the platform's path. Shells and Swift never see Room types. Entities and DAOs are public so `AppDatabase` can list them, and `@HiddenFromObjC` keeps them out of the iOS framework. DAO functions are `suspend` and run on the database's IO context. The boundary is [data-access.md](data-access.md).

SQLDelight is not used. It was replaced once Room 3 became stable with KMP support.

## Networking — Ktor client

`io.ktor:ktor-client-core` with ContentNegotiation. JSON is `kotlinx-serialization-json`.

Engines: OkHttp on Android, Darwin on iOS, CIO on desktop. Request and response types stay in `data`. The boundary is [data-access.md](data-access.md).

## Dependency injection — Koin

`io.insert-koin:koin-core`. `:sharedLogic` starts the graph and is the only place that resolves from it. Each `:feature:` module depends on it only for its one `<Name>Module.kt` file, which declares bindings. `io.insert-koin:koin-test` checks the module graph in `:sharedLogic` tests.

Screens, use cases, repositories, and SwiftUI take constructor arguments. Do not call `get`, `inject`, or `koinInject` from a feature, a composable, or a Swift view.

Metro and kotlin-inject are not used. Their compile-time contribution across multi-module native targets is still limited, and this app's features are separate modules compiled for iOS.

## Navigation

Android and desktop use Navigation Compose, `org.jetbrains.androidx.navigation:navigation-compose`, in `:sharedUI`. iOS uses SwiftUI `NavigationStack` in `iosApp`.

The shell owns the routes. A feature does not navigate into another feature. Voyager and Decompose are not used: iOS screens stay SwiftUI, so there is no shared navigator.
