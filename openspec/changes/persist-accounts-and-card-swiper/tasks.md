# Tasks

## 1. Dependencies and `:core`

- [x] 1.1 Add SQLDelight (Gradle plugin, runtime, Android / native / JDBC SQLite drivers), `kotlinx-coroutines-core`, `koin-core` and `koin-test` to `docs/system/stack.md` and `gradle/libs.versions.toml`, choosing the newest SQLDelight and Koin releases compatible with Kotlin 2.4.20; verify `./gradlew help` resolves the catalog.
- [x] 1.2 Create `:core` (same targets as the features, stdlib only) with `Outcome` and `Reason` as in `docs/system/errors.md`, include it in `settings.gradle.kts`, add a `commonTest` covering `Ok` / `Failed`; verify `./gradlew :core:allTests` passes.
- [x] 1.3 Make `:sharedLogic` depend on and export `:core` to the `SharedLogic` framework; verify `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` succeeds and `OutcomeOk` appears in the generated header.

## 2. Accounts storage in `:feature:accounts`

- [x] 2.1 Apply the SQLDelight plugin to `:feature:accounts` and add `finance_account.sq` (table from design §1, `insert`, `lastInsertRowId`, `selectAll ORDER BY id`); verify the SQLDelight generate task produces the queries class.
- [x] 2.2 Add `SavedAccount` and change `AccountRepository` to `suspend add(...): Outcome<SavedAccount>` and `suspend all(): Outcome<List<SavedAccount>>`; add an `accountSummary` overload for saved `AccountDetails` with tests in `AccountSummaryTest`; verify `./gradlew :feature:accounts:allTests` compiles and the summary tests pass.
- [x] 2.3 Implement the SQLDelight-backed `AccountRepositoryImpl(driver)` with row ↔ domain mapping via stable enum codes, insert in a transaction returning the new id, failures mapped to `Reason.Unavailable` and `CancellationException` rethrown; change `createAccountRepository(driver)`. Rewrite `AccountRepositoryTest` against an in-memory JDBC driver (jvmTest): round-trip for each of Bank / Investment / Loan, order by creation, unknown enum code → `Failed`, closed driver → `Failed`; verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.4 Make `AddAccount` `suspend`, add `AddAccountResult.Failed(reason)` and return `Created(savedAccount)`; update `AddAccountTest` with a fake repository covering success, invalid form and storage failure; verify `./gradlew :feature:accounts:allTests` passes.

## 3. Composition root with Koin and shells

- [x] 3.1 In `:sharedLogic` declare `AppDatabase` with `dependency(project(":feature:accounts"))` and add per-platform driver helpers (Android `Context`, desktop app-data file path, iOS fixed file name); verify `./gradlew :sharedLogic:compileKotlinJvm :sharedLogic:linkDebugFrameworkIosSimulatorArm64` succeed.
- [x] 3.2 Add the Koin modules (`databaseModule(driver)` creating the schema when new, `accountsModule` binding `AccountRepository` via `createAccountRepository`), a guarded `startTakt(driver)`, and `AppDependencies : KoinComponent` exposing `accountRepository` (design §2). Add jvmTests: `koin-test` `verify()` over the modules, and start Koin on an in-memory driver → save and read an account → `stopKoin()`; verify `./gradlew :sharedLogic:jvmTest` passes.
- [x] 3.3 Android: add `TaktApp : Application` (registered in the manifest) calling `startTakt` in `onCreate`; `MainActivity` passes `AppDependencies()` to `App(deps, contrast)`. Desktop: call `startTakt` in `main`. Remove the process-wide `lazy` from `App.kt`; confirm no composable calls `koinInject`/`koinViewModel`; verify `./gradlew :androidApp:assembleDebug` builds and `./gradlew :desktopApp:run` starts and Add Account still saves.
- [x] 3.4 iOS: call `startTakt` from `iOSApp.init` and pass `AppDependencies().accountRepository` to `AddAccountView` and `HomeView` instead of creating the repository in `ContentView`; verify the app builds and launches in the iOS simulator.

## 4. Add Account save flow

- [x] 4.1 Compose: `AddAccountViewModel.save()` launches in `viewModelScope`, sets `saving` (blocks `canSave`), on `Failed` sets `saveFailed` shown as "Couldn't save the account. Try again." and cleared on edit; extend `AddAccountViewModelTest` for success, failure-keeps-entries, retry and double tap; verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 4.2 iOS: `AddAccountModel` awaits the `suspend` save with the same `saving` / `saveFailed` rules and `AddAccountView` shows the message; verify in the simulator that saving works and a double tap creates one account.

## 5. Shared account card and Add Account layouts

- [x] 5.1 Compose: extract a display-only `AccountCard` (badge/icon, name, amount slot, last 4, summary) from `AccountPreviewCard`; the preview passes its editable amount + currency picker into the slot; verify the Add Account preview looks unchanged on a phone and `./gradlew :sharedUI:jvmTest` passes.
- [x] 5.2 iOS: extract `AccountCardView` with an amount slot from the `AddAccountView` preview the same way; verify the Add Account preview looks unchanged on iPhone.
- [ ] 5.3 Compose: Add Account chooses its layout from the available size (design §9): phone column, centred ≤ 560 dp column on medium, preview-beside-form with fixed preview and button under the form on wide, single scrolling column when height is short; verify on an Android phone (portrait and landscape), a tablet or foldable emulator, and a resized desktop window against the four Add Account layout requirements in `specs/finance-account-creation/spec.md`.
- [ ] 5.4 iOS: the same Add Account layouts in SwiftUI by available width and height; verify on iPhone (portrait and landscape), iPad portrait, iPad landscape and iPad Split View against the same requirements.

## 6. My Finances swiper

- [x] 6.1 Extend `HomeState` / `HomeIntent` / `HomeViewModel` with the My Finances content (`Loading`, `Failed`, `Empty`, `Accounts(cards, selectedId)`), intents `Shown`, `AccountSelected`, `PreviousTapped`, `NextTapped`, `RetryTapped`, `AddFirstAccountTapped`, and the selection rule from design §6; `HomeViewModel` takes the `AccountRepository` from `AppDependencies` via `App`. Add `HomeViewModelTest` covering empty, failure + retry, first load selects first, new account selected after save, cancel keeps selection, deleted selection falls back to first, previous/next at both ends; verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 6.2 Compose phone layout: replace the My Finances placeholder with `HorizontalPager` (peeking neighbours, snapping), page dots hidden for one account, "Recent activity" / "No transactions yet" below, "Add your first account" card, failure message with "Try again"; send `Shown` on `ON_START`; verify on an Android phone against the swiper, card, indicator, activity, empty, selection and failure scenarios in `specs/finance-accounts-overview/spec.md`.
- [ ] 6.3 Compose wide layouts and controls: centred ≤ 480 dp column on medium, two panes (swiper ~420 dp + Recent activity) when the available width fits, single centred card for empty / failure, previous / next buttons and left/right arrow keys on desktop and wide panes (design §8–9); verify on a tablet emulator (portrait and landscape) and a desktop window resized across the threshold against the medium, two-pane and "moving between cards without swiping" requirements.
- [x] 6.4 iOS: extend `HomeModel` with the same content states, selection rule and previous/next, reload in `.task` on My Finances; build the snapping `ScrollView` swiper (`.viewAligned`, `.contentMargins`, `.scrollPosition(id:)`), dots, activity area, empty and failure states; verify on iPhone against the phone scenarios.
- [ ] 6.5 iOS wide layouts: measure the `NavigationSplitView` detail column, show the centred column or two panes by available width, previous/next buttons on iPad regular width and arrow keys with a hardware keyboard; verify on iPad portrait, landscape and Split View against the same requirements as 6.3.

## 7. Integration checks

- [ ] 7.1 On an Android phone, an Android tablet or foldable, desktop, iPhone and iPad: add a Bank, an Investment and a Loan account, confirm each lands selected, kill and relaunch the app, and confirm all three cards are back in order with their details and the layout matches the device size.
- [x] 7.2 Run `./gradlew allTests` and `openspec validate persist-accounts-and-card-swiper --strict`; verify both pass.

## Workflow follow-up

- Archive the change with `/opsx:archive` after review.
