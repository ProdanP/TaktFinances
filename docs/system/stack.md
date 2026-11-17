# Stack

Versions live in `gradle/libs.versions.toml`. This file is the set of allowed choices. Add a library here before adding it to the version catalog. Add the dependency to a module only when a type in that module uses it.

`domain` stays on the Kotlin standard library and `kotlinx-datetime`. Room and Ktor are `data` tools. Koin may be used by any module that needs it; only `:sharedLogic` starts the graph. `domain` and `data` packages stay free of it.

## In use

- Kotlin Multiplatform for Android, iOS (`iosArm64`, `iosSimulatorArm64`), and desktop (JVM 11).
- Compose Multiplatform and Material 3 for Android and desktop UI.
- Compose Material Icons Extended (`org.jetbrains.compose.material:material-icons-extended`, 1.7.3) for navigation and screen icons on Android and desktop.
- Material 3 adaptive (`org.jetbrains.compose.material3.adaptive:adaptive-navigation3` and `org.jetbrains.compose.material3:material3-adaptive-navigation-suite`) for the list-detail scene and the navigation bar, rail or drawer on Android and desktop. Inside a screen, `widthClass(maxWidth)` in `:sharedUI` picks Compact, Medium or Wide from the space the screen gets.
- SwiftUI for iOS UI. The iOS app imports the `SharedLogic` framework. Charts on iOS use Swift Charts (system framework); Android and desktop draw charts with Compose `Canvas`, with no chart library.
- kotlinx-datetime (`org.jetbrains.kotlinx:kotlinx-datetime`) for calendar dates (`LocalDate`), months (`YearMonth`) and today's date in the device time zone. It is the one library `domain` may use besides the standard library and `:core`; `:feature:accounts` exposes it with `api`, so `SharedLogic` exports its types to Swift.
- AndroidX Activity Compose for the Android entry. AndroidX Lifecycle ViewModel and Runtime Compose for Compose state holders.
- kotlinx.coroutines, including the Swing dispatcher on desktop. `kotlinx-coroutines-core` is a `data` dependency of a feature that moves blocking work off the caller's thread.
- kotlinx-coroutines-test for coroutine and Flow tests in `commonTest`.
- kotlin-test, JUnit, and AndroidX test (Espresso) for tests.
- Compose UI test (`org.jetbrains.compose.ui:ui-test`) for screen tests in `:sharedUI` `jvmTest`. They run on desktop, so they need no device or Robolectric.

## Persistence — Room

`androidx.room3` 3.0.x (Gradle plugin, `room3-runtime`, and `room3-compiler` through KSP) with `androidx.sqlite:sqlite-bundled`. One SQLite database for the app, the same bundled SQLite on Android, iOS and desktop.

Each feature owns the `@Entity` and `@Dao` types for its tables, in its `data.local` package, and depends on `room3-runtime` only. `:sharedLogic` declares the one `@Database` (`AppDatabase`, internal), lists every feature's entities, runs `room3-compiler` on `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` and `kspJvm`, and commits the exported schema under `sharedLogic/schemas/`. That schema file is the reviewed record of the database and what an auto-migration diffs against.

The shells start the app through `startTaktOnAndroid(context)`, `startTaktOnDesktop()` and `startTaktOnIos()`, which open the database at the platform's path. Shells and Swift never see Room types. Entities and DAOs are public so `AppDatabase` can list them, and `@HiddenFromObjC` keeps them out of the iOS framework. DAO functions are `suspend` and run on the database's IO context. The boundary is [data-access.md](data-access.md).

SQLDelight is not used. It was replaced once Room 3 became stable with KMP support.

## Networking — Ktor client

`io.ktor:ktor-client-core` with ContentNegotiation. JSON is `kotlinx-serialization-json`.

Engines: OkHttp on Android, Darwin on iOS, CIO on desktop. Request and response types stay in `data`. The boundary is [data-access.md](data-access.md).

## Dependency injection — Koin

`io.insert-koin:koin-core`. `:sharedLogic` starts the graph. Any module may depend on Koin when it needs to declare bindings or resolve from the graph. Each `:feature:` module uses it for its one `<Name>Module.kt` file. `io.insert-koin:koin-test` checks the module graph in `:sharedLogic` tests.

`io.insert-koin:koin-compose-viewmodel` in `:sharedUI`: `presentationModule` declares the ViewModels and Compose screens get them with `koinViewModel()`.

Use cases and repositories take constructor arguments. Swift views do not call Koin; they read `AppDependencies`.

Metro and kotlin-inject are not used. Their compile-time contribution across multi-module native targets is still limited, and this app's features are separate modules compiled for iOS.

## Navigation

Android and desktop use Navigation 3, `org.jetbrains.androidx.navigation3:navigation3-ui`, in `:sharedUI`, with `lifecycle-viewmodel-navigation3` so each entry gets its own ViewModel store. The back stack is a `NavBackStack` from `rememberNavBackStack`, so it survives process death. iOS uses SwiftUI `NavigationStack` in `iosApp`.

The shell owns the routes. A feature does not navigate into another feature. Voyager and Decompose are not used: iOS screens stay SwiftUI, so there is no shared navigator.
