# Design

## Context

See proposal.md for why. Current state:

- `:feature:accounts` applies the SQLDelight plugin and owns `FinanceAccount.sq` (one `finance_account` table, plus `insert`, `lastInsertRowId` and `selectAll`). `AccountRows.kt` maps the generated `Finance_account` row to and from the domain. `AccountRepositoryImpl` takes a `SqlDriver`, wraps each call in `withContext(Dispatchers.IO)`, and maps any exception to `Reason.Unavailable`.
- `:sharedLogic` declares the empty `AppDatabase` with `dependency(project(":feature:accounts"))`, holds the Koin graph (`startTakt(driver)`), and has one driver helper per platform. The shells call `startTakt(androidDatabaseDriver(this))` and `startTakt(jvmDatabaseDriver(desktopDataDirectory()))`. iOS calls `TaktIosKt.startTaktOnIos()`.
- Tests: `AccountRepositoryTest` (feature `jvmTest`, in-memory JDBC driver, raw `UPDATE` statements to corrupt rows) and `TaktGraphTest` (sharedLogic `jvmTest`, graph checks plus reopening a file database).
- `domain` stays on the standard library, and the repository API (`add`, `all` returning `Outcome`) does not change.

## Goals / Non-Goals

**Goals:**
- One Room 3 database for the app, using the same bundled SQLite on Android, iOS and desktop.
- A feature still owns its tables (entity and DAO) and receives storage from the composition root. No database type crosses into `domain`, composables or Swift.
- The same table shape, stored codes and repository behaviour, including `Unavailable` for unreadable rows and a closed store.
- Keep every existing repository and graph test scenario, ported to Room.

**Non-Goals:**
- Reactive reads (`Flow` DAOs). The reload-on-show decision in `persist-accounts-and-card-swiper` stands.
- Migrations or auto-migrations. The schema starts at version 1. Exporting it now only makes a future auto-migration possible.
- Carrying over data from the SQLDelight file.
- Room 2 (`androidx.room`), which is in maintenance mode.

## Decisions

### 1. Room 3.0.x stable with the bundled driver everywhere

Use `androidx.room3` (runtime, compiler, Gradle plugin) at the newest 3.0.x patch (3.0.3 when written), and `androidx.sqlite:sqlite-bundled` at the version Room 3.0.x expects. Build every platform with `BundledSQLiteDriver()`.

*Alternatives:* the platform drivers (`AndroidSQLiteDriver`, `NativeSQLiteDriver`) would avoid shipping SQLite, but then Android, iOS and desktop would run different SQLite versions. A 3.1.0 alpha is rejected for the same reason `stack.md` once rejected Room: it's not stable.

### 2. Entity and DAO live in the feature; `@Database` lives in `:sharedLogic`

```
:feature:accounts  data/FinanceAccountEntity  (@Entity "finance_account")
                   data/FinanceAccountDao     (@Dao: insert, selectAll)
        ^ api
:sharedLogic       db/AppDatabase  @Database(entities=[FinanceAccountEntity], version=1)
                                   @ConstructedBy(AppDatabaseConstructor)
                                   abstract fun financeAccountDao(): FinanceAccountDao
                   KSP: room3-compiler on kspAndroid, kspIosArm64, kspIosSimulatorArm64, kspJvm
```

This keeps the SQLDelight split: each feature owns its tables, and only the composition root knows the full database. The feature depends only on `room3-runtime` for the annotations. `FinanceAccountEntity` and `FinanceAccountDao` must be public because `AppDatabase` in another module refers to them. That is the same known gap as SQLDelight's public generated types: they stay in the `data` package and are never returned from the repository.

*Alternative:* put `@Database` in the feature. Rejected as the target design because a second feature with tables would need a second database, which `data-access.md` forbids. It remains the fallback if KSP on Kotlin/Native cannot read DAO annotations from a dependency klib (see Risks). Task 1 checks this before anything else depends on it.

*Spike outcome (task 1.2):* with Room 3.0.3 and KSP 2.3.13 on Kotlin 2.4.20, `:sharedLogic` generated `FinanceAccountDao_Impl` from the feature's DAO for `jvm`, `android` and `iosSimulatorArm64`, and the iOS framework linked. The target design stands, so the fallback is not needed. The Room database is in package `com.prodan.taktfinances.database`, because SQLDelight's generated `com.prodan.taktfinances.db.AppDatabase` exists until task 3.5.

### 3. Same columns, same codes

`FinanceAccountEntity` maps to table `finance_account` with the current snake_case column names (`@ColumnInfo(name = "amount_minor")` and so on), `@PrimaryKey(autoGenerate = true) val id: Long`, and nullable detail columns. `AccountRows.kt` changes its row type from `Finance_account` to `FinanceAccountEntity`. The code maps and the `UnreadableRowException` rules are unchanged. Money stays `Long` minor units in an `INTEGER` column.

### 4. The DAO is suspend-only, and the repository drops its own dispatcher

```kotlin
@Dao interface FinanceAccountDao {
    @Insert suspend fun insert(row: FinanceAccountEntity): Long   // the new row id
    @Query("SELECT * FROM finance_account ORDER BY id") suspend fun selectAll(): List<FinanceAccountEntity>
}
```

`@Insert` returns the rowid, so `lastInsertRowId` and the explicit transaction go away. The builder sets `setQueryCoroutineContext(Dispatchers.IO)`, so `AccountRepositoryImpl` no longer takes `io` or calls `withContext`. Its `storeCall` keeps the try/catch: rethrow `CancellationException`, otherwise return `Failed(Unavailable)`. Mapping a row still happens inside `storeCall`, so an unreadable row is still `Unavailable`. `createAccountRepository(dao: FinanceAccountDao)` replaces `createAccountRepository(driver: SqlDriver)`.

*Alternative:* keep `withContext(io)` for symmetry. Rejected because it switches dispatchers twice for nothing. Tests that need determinism use `runTest` with Room's own context, which already works with suspend DAOs.

### 5. Shells start the app through one function per platform; `AppDatabase` stays internal

`:sharedLogic` exposes `startTaktOnAndroid(context)`, `startTaktOnDesktop()` (which uses `desktopDataDirectory()`) and the existing `startTaktOnIos()`. Each builds the database with `Room.databaseBuilder<AppDatabase>(...)` at its platform path, then calls an `internal fun startTakt(database: AppDatabase)`. `AppDatabase`, its constructor object and the builders are `internal`, so neither the shells nor the Swift framework header see Room. Koin holds `single<AppDatabase>` and `single { get<AppDatabase>().financeAccountDao() }`. `TaktGraphTest` calls the internal `startTakt` with an in-memory database.

*Alternative:* keep `startTakt(database)` public and let shells pass a database, as they pass a driver today. Rejected because `AppDatabase` would become public API of the iOS framework and of every shell module.

### 6. New file name, and the old file is deleted

The Room file is `taktfinances.db`, in the same directories as before: Android's app database directory, desktop's `desktopDataDirectory()`, and iOS Application Support. A new name means Room never opens a SQLDelight file. A SQLDelight file has `user_version = 1` and no `room_master_table`, so Room would fail to validate it or misread it. Before building, each platform helper deletes the legacy `takt.db` and its `-journal`, `-wal` and `-shm` files if they exist (Android uses `context.deleteDatabase("takt.db")`). The deletion is idempotent and can be removed in a later cleanup.

*Alternative:* keep `takt.db` and use `fallbackToDestructiveMigration`. Rejected because it depends on how Room treats a foreign file with a matching version, and it would leave destructive fallback switched on for future versions.

### 7. Schema export is checked in

`room3 { schemaDirectory("$projectDir/schemas") }` in `:sharedLogic`, with `schemas/.../AppDatabase/1.json` committed. This takes the place of the `.sq` file as the reviewed record of the schema and is what a future auto-migration diffs against.

### 8. Tests

- Feature `jvmTest`: a test-only `@Database(entities = [FinanceAccountEntity])` in `jvmTest` (KSP on `kspJvmTest`) without `@ConstructedBy`, because a jvm-only source set cannot hold both the `expect` and the generated `actual`, and Room on JVM finds `_Impl` by reflection,, built in memory with `BundledSQLiteDriver`, gives `AccountRepositoryTest` a real DAO without depending on `:sharedLogic`. The corrupt-row cases run their `UPDATE` through a writer connection, `useWriterConnection { it.execSQL(...) }`. The closed-store case closes the database. All seven existing scenarios stay.
- `TaktGraphTest`: graph verification, read-back, start-twice, and reopen-the-file, using an in-memory database and a temp-directory database.
- A new jvm test checks that a legacy `takt.db` in the desktop directory is removed and that the new database opens empty.

## Risks / Trade-offs

- [KSP for Room on Kotlin/Native may not read DAO annotations from `:feature:accounts`'s klib] → Task 1 is a spike: build `:sharedLogic` for `iosSimulatorArm64` and `jvm` with one entity and DAO in the feature. If it fails, use the Decision 2 fallback (move `@Database` into `:feature:accounts`, with `:sharedLogic` only building it) and record that in this design.
- [KSP may not support Kotlin 2.4.20 or AGP 9's KMP library plugin] → Pin the KSP release built for 2.4.20 in the same spike. If none works, stop and report rather than downgrading Kotlin.
- [Room entity and DAO types are public in the feature] → Same gap as today. They stay in `data` and are never part of a repository signature.
- [Bundled SQLite makes the iOS and Android apps about 1 MB larger] → Accepted for identical SQLite behaviour on every target.
- [The legacy-file cleanup could point at the wrong iOS path] → Confirm where SQLDelight's `NativeSqliteDriver` put `takt.db` on the simulator before writing the deletion. If it's not found, the deletion does nothing.
- [Room 3 API names differ between doc copies (e.g. `setQueryCoroutineContext` vs `setCoroutineContext`)] → Follow the API reference of the pinned version.

## Migration Plan

1. A spike checks KSP and Room across modules on jvm and iOS simulator.
2. Add Room alongside SQLDelight long enough to port the repository and tests, then remove SQLDelight in the same change. No release goes out with both.
3. On first launch of the new build, each platform deletes `takt.db` and creates `taktfinances.db` at version 1.
4. Rollback means reverting the change. A user who goes back to a SQLDelight build gets a fresh `takt.db`. Data saved under Room is not visible to the old build, which is acceptable for development data.
