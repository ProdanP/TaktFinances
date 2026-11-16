# Design

## Context

- `AccountRepositoryImpl` is an in-memory list with synchronous `add` / `all`. `App.kt` creates it in a process-wide `lazy`; `ContentView.swift` creates its own.
- `docs/system/stack.md` already chooses SQLDelight (one database, drivers created in the shells, each feature owns its `.sq` files) but nothing is installed. There is no `:core` module, so `Outcome` from `docs/system/errors.md` does not exist yet.
- `docs/system/architecture.md`: `:sharedLogic` is the composition root; shells pass platform handles in; `domain` depends on the Kotlin standard library only.
- `:sharedUI` depends on `:sharedLogic` with `api`, so Compose can receive what `:sharedLogic` builds.
- `AccountPreviewCard` (Compose) takes `AddAccountState` and embeds the editable amount input and currency picker, so it cannot be shown for a saved account as is. The iOS preview in `AddAccountView` has the same shape.
- iOS deployment target is 18.2.
- `docs/system/adaptive-ui.md`: every screen must work on phones, tablets, foldables and desktop; layout adapts in the view, never in the ViewModel or Swift model. Today Add Account is one centred column (480 dp Compose / 520 pt iOS) at every size, and `HomeScreen` switches between bottom bar, rail and permanent drawer by window width class.
- Koin is allowed only in `:sharedLogic`; features, composables and Swift views must not call `get`, `inject` or `koinInject` (`stack.md`, `architecture.md`).

## Goals / Non-Goals

**Goals:**
- One SQLite database for the app, opened once, with accounts in one table owned by `:feature:accounts`.
- The same repository instance on Android, desktop and iOS, built in `:sharedLogic`.
- The swiper and the activity area built from the same domain data on Compose and SwiftUI.

**Non-Goals:**
- Live observation of the table (no `Flow` from the repository).
- `koinViewModel()` / `koinInject()` in composables, or Koin in feature modules.
- A schema migration (this is version 1 of the schema).
- Presenting Add Account as a dialog or side sheet on wide windows.
- Any per-account data other than the account itself.

## Decisions

### 1. One table with a type column and nullable detail columns

```
CREATE TABLE finance_account (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  type          TEXT NOT NULL,        -- BANK | INVESTMENT | LOAN
  name          TEXT NOT NULL,
  currency      TEXT NOT NULL,        -- Currency enum name
  amount_minor  INTEGER NOT NULL,
  bank          TEXT, bank_kind TEXT, last4 TEXT,      -- bank only
  investment_kind TEXT, platform TEXT,                -- investment only
  loan_direction TEXT, counterparty TEXT              -- loan only
);
```

Enums are stored by a stable code owned by `data` (not `ordinal`, not display titles), so renaming a title does not break stored rows. The mapper turns a row into `AccountDetails`; a row whose required detail columns are null or whose codes are unknown is a read failure (`Reason.Unavailable`), not a crash.

Order is `ORDER BY id`: autoincrement ids give creation order without a clock in `domain`.

*Alternative:* a table per type joined to a common table. Rejected: every read needs a join or three queries, and three small types do not justify it.

### 2. Database and module wiring with Koin

```
androidApp: TaktApp.onCreate --> startTakt(androidDatabaseDriver(context))
desktopApp: main ------------->  startTakt(jvmDatabaseDriver(appDataFile))
iosApp:     iOSApp.init ------>  startTakt(nativeDatabaseDriver())
                                        |
                                        v
 :sharedLogic   startTakt(driver) = startKoin { modules(databaseModule(driver), accountsModule) }
                  databaseModule:  single<SqlDriver> { driver }
                                   single { AppDatabase.Schema.create-if-new; AppDatabase(get()) }
                  accountsModule:  single<AccountRepository> { createAccountRepository(get()) }
                AppDependencies : KoinComponent        <- the only class that resolves
                  val accountRepository: AccountRepository by inject()
                                        |
                       +----------------+----------------+
                       v                                 v
   :sharedUI App(deps) -> viewModel {                iosApp: let deps = AppDependencies()
     HomeViewModel(deps.accountRepository) }           HomeModel(repository: deps.accountRepository)
```

- SQLDelight multi-module setup: `:feature:accounts` applies the plugin and holds `finance_account.sq`; `:sharedLogic` declares the app database `AppDatabase` with `dependency(project(":feature:accounts"))` and owns `Schema`. The feature constructs its generated queries class from the `SqlDriver` it receives, so the feature never imports `AppDatabase` (which would be a cycle).
- Driver helpers live in `:sharedLogic` platform source sets so the shells don't depend on SQLDelight directly: Android takes a `Context`, desktop a file under the user's app-data directory, iOS uses the native driver with a fixed file name.
- **Koin modules live only in `:sharedLogic`.** The feature stays Koin-free; `accountsModule` calls the feature's public `createAccountRepository(driver)` factory. Repositories are `single`, so Compose and SwiftUI share one instance per process.
- **One resolve point.** `AppDependencies : KoinComponent` in `:sharedLogic` exposes typed properties. `App(deps: AppDependencies, contrast)` passes them into `viewModel { ... }` factories, and Swift reads them the same way, so composables and Swift views never call `get`, `inject`, `koinInject` or `koinViewModel` (keeps `architecture.md` intact). Adding a dependency later means a binding in a module plus a property on `AppDependencies`.
- **Start once.** `startTakt` is idempotent-guarded (`GlobalContext.getOrNull()`), called from `TaktApp.onCreate` on Android (so it survives configuration changes and is rebuilt after process death), from `main` on desktop, and from `iOSApp.init` on iOS. The process-wide `lazy` in `App.kt` and the repository creation in `ContentView` are removed.
- **Tests:** a `koin-test` `verify()` check on the module list in `:sharedLogic` jvmTest, plus a test that starts Koin with an in-memory JDBC driver, saves and reads an account, and stops Koin.

*Alternatives:* a hand-written `AppGraph` class (simpler for one repository, but the project wants Koin as the graph grows); `koinViewModel()` in composables (shorter, but forbidden by `architecture.md` and has no Swift equivalent); each feature with its own database file (rejected by `stack.md`).

### 3. `:core` with `Outcome`

New `:core` module, Kotlin standard library only, with `Outcome` and `Reason` exactly as in `errors.md`. `:feature:accounts` depends on it; `:sharedLogic` exports it to Swift.

### 4. Repository: `suspend`, `Outcome`, no `Flow`

```kotlin
interface AccountRepository {
    suspend fun add(account: FinanceAccount): Outcome<SavedAccount>
    suspend fun all(): Outcome<List<SavedAccount>>
}
data class SavedAccount(val id: Long, val account: FinanceAccount)
```

Calls run on the database dispatcher inside the implementation (`Dispatchers.IO` on JVM/Android, `Dispatchers.Default` on native — the feature's `data` adds `kotlinx-coroutines-core`). Exceptions map to `Outcome.Failed(Reason.Unavailable)`; `CancellationException` is rethrown.

My Finances reloads with `all()` each time it becomes visible instead of observing the table.

*Alternative:* `Flow<List<SavedAccount>>` via SQLDelight coroutines extensions. Rejected: `domain` would depend on kotlinx.coroutines, and Swift cannot collect a `Flow` without SKIE (ruled out by `errors.md`), so iOS would need a hand-written callback bridge. The only writer today is Add Account, which always returns to My Finances, so reload-on-show gives the same result. Revisit when transactions change balances in the background. (This replaces the "watch the list" default floated during exploration.)

### 5. `AddAccount` becomes `suspend`

`AddAccountResult` gains `Failed(reason)`. The use case validates, builds the account and awaits `repository.add`. `Created` carries the `SavedAccount`.

The Add Account state holders track `saving: Boolean`; `canSave` is false while saving (no double save), and a failure sets `saveFailed = true`, shown as "Couldn't save the account. Try again." and cleared on the next edit or save.

### 6. Selecting the new card

`HomeViewModel` / `HomeModel` keep `selectedAccountId`. On each reload:

- if the reloaded list contains an id not seen in the previous load → select the highest new id (the account just saved);
- else if the previous selection still exists → keep it;
- else → select the first card.

This needs no message between Add Account and Home; it also handles the first load (no previous list → first card). Storing the id rather than the index keeps the selection correct if ordering changes later.

### 7. My Finances state

```
MyFinancesContent (part of HomeState)
  Loading
  Failed                       -> "Couldn't load your accounts" + Try again
  Empty                        -> "Add your first account" card
  Accounts(cards, selectedId)  -> swiper + indicator + Recent activity
```

Home intents gain `Shown` (reload), `AccountSelected(id)`, `RetryTapped` and `AddFirstAccountTapped` (same effect as "+"). Compose sends `Shown` from a lifecycle `ON_START` effect; iOS from `.task` on the My Finances view, which re-runs on return from Add Account.

Card display values (badge/icon, formatted amount, summary, `•••• 1234`) come from the existing domain helpers (`accountSummary`, amount formatting) so card and preview always agree. `accountSummary` takes a draft today; add an overload for saved `AccountDetails` in `domain`.

### 8. Swiper UI

- **Compose:** `HorizontalPager` with horizontal `contentPadding` (~32 dp) and `pageSpacing` (~12 dp) so neighbours peek; `snapshotFlow { settledPage }` sends `AccountSelected`; when state selects a different id, `animateScrollToPage`. Dots are a small `Row` (a ready-made indicator is not in Material 3).
- **Previous / next controls:** shown when the pane is two-pane wide or a fine pointer is present (Compose: `LocalInputModeManager` / pointer hover on desktop, always on JVM; iOS: regular width or a connected pointer via `.onContinuousHover` availability, always on iPad regular width). Buttons send `AccountSelected` for the neighbour id. Left/right arrow keys use `onPreviewKeyEvent` on the swiper (focusable) in Compose and `.onKeyPress(.leftArrow/.rightArrow)` in SwiftUI. The pager does not rely on mouse drag on desktop.
- **SwiftUI:** horizontal `ScrollView` + `LazyHStack` with `.scrollTargetLayout()`, `.scrollTargetBehavior(.viewAligned)`, `.contentMargins(.horizontal, 32)` and `.scrollPosition(id:)` bound to the selected id. Dots as an `HStack`.

### 9. Layouts by available size

Both screens choose their layout from the size **available to the screen content** (`BoxWithConstraints` in Compose; `GeometryReader` / `ViewThatFits` or container width in SwiftUI), not the window size class alone: at an 840 dp window the permanent drawer leaves ~480 dp, which cannot hold two panes. The ViewModel and Swift model stay layout-agnostic (`adaptive-ui.md`).

| Available width | My Finances | Add Account |
|---|---|---|
| < 600 dp (phone) | stacked: swiper, dots, Recent activity | one scrolling column, preview on top, button pinned at bottom |
| 600 – 839 dp | stacked, centred column ≤ 480 dp | one centred column ≤ 560 dp |
| ≥ 840 dp | two panes: swiper pane ~420 dp + Recent activity fills the rest | two columns: preview ~360 dp (fixed) + form column ≤ 560 dp (scrolls), button under form |

- **Short height** (available height < ~480 dp, e.g. phone landscape): Add Account falls back to the single scrolling column even when wide, so the preview does not pin and hide the fields.
- **Empty / failure states** on My Finances render as one centred card at every width, without a second pane.
- **Insets:** both layouts pad to safe-drawing insets, not fixed offsets; the pinned Add Account button respects the IME and navigation bar insets.
- **iOS:** the same thresholds in points. My Finances lives in the `NavigationSplitView` detail on iPad, so it measures the detail column, not the screen. Add Account is pushed on the `NavigationStack` and measures the full width.
- Thresholds are constants in the UI layer of each platform (one place per platform), so tuning them does not touch state holders.
- **Shared card:** extract a display-only `AccountCard` (Compose) / `AccountCardView` (SwiftUI) holding the badge, name, summary and last-4 layout, with an amount slot. The Add Account preview passes its editable amount + currency picker into the slot; the swiper passes static text. This keeps the two looks identical without coupling the swiper to `AddAccountState`.

## Risks / Trade-offs

- [SQLDelight generated query classes are public, while `data-access.md` wants query types internal] → Keep them in a `data` package, never return them, and check whether the SQLDelight version in use supports internal generation; if not, note it as a known gap rather than wrapping every query.
- [SQLDelight / Kotlin 2.4.20 compatibility] → Pick the newest SQLDelight release that supports the project's Kotlin version and verify with a desktop + iOS simulator build before writing the repository.
- [Reload-on-show reads the whole table every time Home appears] → Fine at personal-finance scale (tens of rows); revisit with transactions.
- [Android process death or activity recreation] → Koin is started in `Application.onCreate`, not the activity, so it is rebuilt with the process and kept across configuration changes.
- [Pager and state disagree during a fling] → State changes only on settled page; programmatic scroll only when ids differ.
- [Koin started twice (Android activity recreation, desktop preview, tests)] → `startTakt` returns early when Koin is already running; tests call `stopKoin()` in teardown.
- [Koin resolution errors only appear at runtime] → `koin-test` `verify()` in jvmTest catches missing bindings at build time.
- [Layout thresholds feel wrong on a specific device (foldable half-open, iPad Split View)] → Thresholds measure available space and live in one constant per platform; check foldable and iPad Split View in the integration pass.
- [Arrow-key navigation steals keys from text fields] → Key handling is scoped to the focused swiper, not the window.

## Migration Plan

No user data exists to migrate: the in-memory list is lost on restart today. The first launch with this change creates the database with schema version 1. Rollback is reverting the change; the database file stays on disk unused.
