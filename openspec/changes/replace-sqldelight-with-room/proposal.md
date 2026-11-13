# Proposal

## Why

`docs/system/stack.md` rules out Room because Room 3 was still alpha. That is no longer true. Room 3.0 (`androidx.room3`) has been stable since July 2026 and supports Android, iOS (`iosArm64`, `iosSimulatorArm64`) and desktop JVM, which are the app's three targets. Persistence is still small (one table, three queries, one repository), so switching to Room now costs the least it ever will. Room also brings annotation-checked DAOs, schema export with auto-migrations, and suspend DAOs that run on Room's own IO context.

## What Changes

- Replace SQLDelight with Room 3 on Android, iOS and desktop, using the bundled SQLite driver (`androidx.sqlite:sqlite-bundled`) on all three.
- `:feature:accounts` owns a `finance_account` entity and a DAO in place of `FinanceAccount.sq` and the generated `FinanceAccountQueries` and `Finance_account` types. Stored codes, columns, and the minor-unit `INTEGER` amount stay the same.
- `:sharedLogic` declares the one `@Database` (`AppDatabase`), builds it per platform, and passes the account DAO to the accounts factory. `createAccountRepository` takes the DAO instead of a `SqlDriver`.
- Shell entry points (`TaktApp`, desktop `main`, `startTaktOnIos`) open the database through new `:sharedLogic` helpers. Swift still sees no database types.
- **BREAKING (local data):** Room opens a new database file and starts at schema version 1. Accounts saved with the SQLDelight build are not carried over. This was decided because the app has no data worth keeping yet. The old `takt.db` file is deleted on first launch.
- Remove the SQLDelight plugin, runtime and drivers from the version catalog and from every build file.
- Update `docs/system/stack.md`, `docs/system/data-access.md`, `docs/system/errors.md` and the `data-access` skill so they describe Room and no longer forbid it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This only replaces the storage library. The "Accounts persist across restarts" requirement from `persist-accounts-and-card-swiper` still holds, with the same order, ids and details. The only exception is the one-time loss of data saved before the switch, which is development data. The change sets `skip_specs: true`.

## Impact

- **Code:** `feature/accounts` (`data/` package, factory, `build.gradle.kts`, `jvmTest` repository test), `sharedLogic` (`di/` in all source sets, `build.gradle.kts`, the `sqldelight/` source folder, `TaktGraphTest`), `androidApp/TaktApp.kt`, `desktopApp/main.kt`, root `build.gradle.kts`.
- **Dependencies:** add `androidx.room3` runtime, compiler and Gradle plugin, `androidx.sqlite:sqlite-bundled`, and the KSP plugin. Remove `app.cash.sqldelight`. KSP must support Kotlin 2.4.20 and AGP 9's `com.android.kotlin.multiplatform.library`.
- **iOS app:** about 1 MB larger because it bundles SQLite. `iOSApp.swift` is unchanged.
- **Docs and skills:** `stack.md`, `data-access.md`, `errors.md`, `.agents/skills/data-access/SKILL.md`.
- **In-flight work:** `persist-accounts-and-card-swiper` has only layout and device-check tasks left. Its design text about SQLDelight goes stale but needs no edit. That change is archived as-is.
