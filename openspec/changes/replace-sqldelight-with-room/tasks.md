# Tasks

## 1. Spike: Room 3 and KSP across modules

- [x] 1.1 Add `androidx.room3` (runtime, compiler, Gradle plugin at the newest 3.0.x), `androidx.sqlite:sqlite-bundled` (the version Room 3.0.x expects) and the KSP plugin release built for Kotlin 2.4.20 to `gradle/libs.versions.toml` and the root `build.gradle.kts` (`apply false`), leaving SQLDelight in place; verify `./gradlew help` resolves the catalog.
- [x] 1.2 Put a throwaway `@Entity` and `@Dao` in `:feature:accounts` (`room3-runtime` only), and an `@Database` with `@ConstructedBy` in `:sharedLogic` with `room3-compiler` on `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` and `kspJvm`. Verify `./gradlew :sharedLogic:compileKotlinJvm :sharedLogic:linkDebugFrameworkIosSimulatorArm64` and the Android library compile all succeed. If Kotlin/Native cannot see the feature's DAO, switch to the design §2 fallback (`@Database` in `:feature:accounts`) and record the outcome under Decision 2 in `design.md`.

## 2. Accounts storage on Room in `:feature:accounts`

- [x] 2.1 Replace the spike types with `FinanceAccountEntity` (table `finance_account`, same snake_case columns, auto-generated `Long` id) and `FinanceAccountDao` (`suspend insert(row): Long`, `suspend selectAll()` ordered by id) in the `data` package (design §3–4); verify `./gradlew :feature:accounts:compileKotlinJvm` succeeds.
- [x] 2.2 Change `AccountRows.kt` to map `FinanceAccountEntity` with the existing code maps and `UnreadableRowException` rules. Change `AccountRepositoryImpl` to take the DAO, drop `io`/`withContext`, `lastInsertRowId` and the explicit transaction, and keep the `CancellationException` rethrow and the `Unavailable` mapping. Change `createAccountRepository(dao)`. Verify the feature compiles for jvm and `iosSimulatorArm64`.
- [x] 2.3 Add a test-only `@Database` in `feature/accounts/src/jvmTest` (`kspJvmTest`) built in memory with `BundledSQLiteDriver`, and port all seven `AccountRepositoryTest` scenarios. The corrupt-row cases use a writer connection for `UPDATE`, and the closed-store case closes the database. Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.4 Remove `FinanceAccount.sq`, the `sqldelight {}` block, the SQLDelight plugin and `sqldelight-runtime`/`sqliteDriver` from `feature/accounts/build.gradle.kts`; verify `./gradlew :feature:accounts:allTests` passes and `grep -r sqldelight feature/accounts` finds nothing.

## 3. App database and startup in `:sharedLogic` and the shells

- [x] 3.1 Add an internal `AppDatabase` (`@Database(entities = [FinanceAccountEntity], version = 1)`, `financeAccountDao()`, `@ConstructedBy` expect object), Room's `schemaDirectory("$projectDir/schemas")`, and commit the generated `1.json`. Verify the schema file appears under `sharedLogic/schemas/` after `./gradlew :sharedLogic:compileKotlinJvm`.
- [x] 3.2 Replace the driver helpers with internal per-platform builders that open `taktfinances.db` with `BundledSQLiteDriver` and `setQueryCoroutineContext(Dispatchers.IO)`, after deleting a legacy `takt.db` and its `-journal`/`-wal`/`-shm` files (design §6). Before writing the iOS deletion, confirm where `NativeSqliteDriver` stored `takt.db` on the simulator. Verify all three targets compile.
- [x] 3.3 Change the Koin graph to `single<AppDatabase>` plus the DAO, and make `startTakt(database)` internal. Add `startTaktOnAndroid(context)` and `startTaktOnDesktop()`, and keep `startTaktOnIos()`. Update `TaktApp.kt` and desktop `main.kt` to call them. Verify `iOSApp.swift` needs no change and the generated `SharedLogic` header has no `AppDatabase` or Room types.
- [x] 3.4 Port `TaktGraphTest` (module verify, read-back, start-twice, reopen-the-file) to in-memory and temp-directory Room databases, and add a test that a legacy `takt.db` in the desktop directory is deleted while the new database opens empty. Verify `./gradlew :sharedLogic:jvmTest` passes.
- [x] 3.5 Remove the SQLDelight plugin, drivers, the `sqldelight/` source folder and the `sqldelight {}` block from `:sharedLogic`, the root build and `gradle/libs.versions.toml`. Verify `grep -ri sqldelight --include=*.kts --include=*.toml --include=*.kt .` (outside `build/` and `openspec/`) finds nothing and `./gradlew build` passes.

## 4. Docs and skill

- [x] 4.1 Update `docs/system/stack.md` (a Room 3 persistence section replaces SQLDelight: bundled driver, entities and DAOs in features, `@Database` in `:sharedLogic`, schema export; remove "Room is not used"), `docs/system/data-access.md` (DAO example, transactions via Room, entity types stay in `data`), `docs/system/errors.md` (Room/SQLite failures) and `.agents/skills/data-access/SKILL.md` (description and checklist name Room, drop "Do not add Room"). Verify `grep -ri sqldelight docs .agents` finds nothing.

## 5. Integration check

- [x] 5.1 On an Android emulator, desktop and the iOS simulator, each with a previous SQLDelight install that has saved accounts: launch the new build and confirm My Finances shows the empty state. Add a Bank, an Investment and a Loan account, kill and relaunch, and confirm all three come back in order with their details. Confirm the old `takt.db` is gone and `taktfinances.db` exists in each platform's data directory.
  - Done with partial coverage, accepted by the user on 2026-11-13. Android emulator (old install): legacy file deleted, empty state, Loan account added through the UI and still there after kill and relaunch. iPhone 16 simulator (old install): legacy file deleted, empty state, a Bank row written with sqlite3 shown correctly after relaunch. Desktop was not launched; jvm tests cover legacy deletion and reopening the file. The full three-account UI flow was not run.

## Workflow follow-up

- Archive `persist-accounts-and-card-swiper` independently; its SQLDelight design notes are historical.
- Archive this change after review.
