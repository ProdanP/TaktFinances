# Tasks

## 1. Dependency

- [x] 1.1 Add `kotlinx-datetime` (0.8.0, the newest stable release, which has `YearMonth`) to `docs/system/stack.md` and `gradle/libs.versions.toml`, add it as an `api` dependency of `:feature:accounts` `commonMain`, and change `docs/system/domain.md` "Dependencies" to allow it (design D-1); note Swift Charts as the iOS chart framework in `stack.md`. Verify `./gradlew :feature:accounts:compileKotlinJvm :sharedLogic:linkDebugFrameworkIosSimulatorArm64` succeeds.

## 2. Domain: tracking model and rules

- [x] 2.1 Add `TrackingMode`, `MonthlyEntry`, `Period` (`Monthly`, `Transactions`), `StartingPoint`, `MonthBalance`, `MonthChange`; change `FinanceAccount` (drop `amountMinor`, add `createdOn`) and `SavedAccount` (add `period`); add top-level `today()` (design D-2, D-3). Verify the module compiles (`./gradlew :feature:accounts:compileKotlinJvm`); callers are fixed in later tasks.
- [x] 2.2 Implement `monthEndBalances`, `SavedAccount.balanceMinor(today)`, `monthChange` and `checkInMonths` with `commonTest` coverage of every scenario in `specs/finance-monthly-tracking/spec.md` "Month-end balance", "Current balance", "Monthly change" and "Months offered for a check-in" (carry-forward, never zero, months before creation, 0 previous balance hides %, created this month, Investment flat at 0.00). Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.3 Add `formatSignedAmount` (U+2212 minus, neutral "0.00"), `formatPercent` ("+15.0%", "−5.0%"), `monthLabel` ("November 2026") and `shortMonthLabel` ("October") with tests, including rounding of 1,530 / 10,810 to "+14.2%". Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.4 Change `AccountRepository` to the design D-5 interface. Update `checkAccountForm` so Investment never requires an amount, and `AddAccount` to take `today` and build a Monthly or Transactions(0) `StartingPoint` (design D-6); update `FormCheckTest` and `AddAccountTest` (Investment with empty amount is valid; Bank in November 2026 starts with entry 2026-11; Investment starts at 0). Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.5 Add `CheckIn` and `DeleteCheckIn` use cases with result types; `commonTest` with a fake repository covering: valid save calls `saveMonthlyEntry` once; future month, month before creation, unparsable amount and an Investment account return `Invalid` without a repository call; storage failure returns `Failed`; delete refuses the start month and a month without an entry. Verify `./gradlew :feature:accounts:jvmTest` passes.

## 3. Data: schema, DAOs, repository

- [x] 3.1 Change `FinanceAccountEntity` (drop `amount_minor`, add `created_on`), add `AccountPeriodEntity`, `MonthlyEntryEntity` (composite primary key, foreign keys with `CASCADE`, index on `account_id`) and `TrackingModeDbo`; and add to `FinanceAccountDao` (upsert entry, delete entry, read active periods, read entries of periods, and the transactional insert) (design D-4, D-5). Verify the jvmTest KSP task generates the test database (`./gradlew :feature:accounts:kspTestKotlinJvm` or the equivalent task name).
- [x] 3.2 Update `FinanceAccountMapper` and add `TrackingMapper` (`toDbo()` / `toDomain()`, ISO date and month text, exhaustive enum `when`s, malformed date throws `UnreadableRowException`); implement the new `AccountRepositoryImpl` methods with `outcomeOf`. Verify the module compiles.
- [x] 3.3 Rewrite `AccountRepositoryTest` (jvmTest, in-memory Room): add Bank/Loan creates one Monthly period with the current-month entry; add Investment creates a Transactions period with opening 0 and no entries; `all` returns oldest first with periods; `get` of an unknown id fails; save entry inserts then replaces the same month; delete removes it; a malformed stored date fails the read; a closed database returns `Failed`. Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 3.4 In `:sharedLogic` list the new entities in `AppDatabase`, bump to version 2, and add `fallbackToDestructiveMigration(dropAllTables = true)` in `buildApp()` with a comment that it must go before the first release; commit the exported `sharedLogic/schemas/.../2.json`. Extend `TaktGraphTest` to save an account and a check-in through the graph and read them back. Verify `./gradlew :sharedLogic:jvmTest` passes and `2.json` lists the three tables.
- [x] 3.5 Add a line to `docs/learnings.md` about the destructive fallback and anything surprising in the Room 3 composite key / foreign key setup. Verify the file has the new line.

## 4. Add Account (Compose and iOS)

- [x] 4.1 Compose: label the amount "Balance this month"; for Investment hide the amount and show the tracking notice, keep the typed amount when switching back, preview shows "0.00" with the currency; `AddAccountViewModel` passes `today()` (injectable in tests). Extend `AddAccountViewModelTest` (Investment saves with empty amount; amount kept across Investment and back). Verify `./gradlew :sharedUI:jvmTest` passes and, on an Android phone, the form matches `specs/finance-account-creation/spec.md` "Amount label", "Investment tracking notice" and "Common form fields".
- [x] 4.2 iOS: the same form changes in `AddAccountModel` / `AddAccountView`. Verify in the iOS simulator that an Investment account saves without an amount and shows 0.00 on its card.

## 5. My Finances (Compose and iOS)

- [x] 5.1 Compose: `AccountCard` shows `balanceMinor(today)`; add `HomeIntent.AccountTapped`, `CheckInTapped`, `SeeAllTapped` and `HomeEffect.OpenAccount(id, checkIn)`; card is clickable (and Enter on the focused card on desktop); `RecentActivity` built from a `models/` UI model: up to 5 newest entries + "See all", "No check-ins yet" + "Check in", or "No trades yet". Extend `HomeViewModelTest` for the new intents and the recent-activity model (start-month only, 8 entries → 5, Investment). Verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 5.2 iOS: same card balance, tap-to-open, and Recent activity in `HomeModel` / `MyFinancesView` / `AccountCardView`. Verify on iPhone against `specs/finance-accounts-overview/spec.md` "Selected account drives the area below" and "Open Account Detail from a card".

## 6. Account Detail (Compose)

- [x] 6.1 Add `AccountDetailState/Intent/ViewModel/Effect` and `models/` (header, chart points, timeline rows) per design D-7: load by id, `Failed` + retry, chart range (default 6M), check-in sheet state (month, amount, saving, failed, delete confirmation), save via `CheckIn`, delete via `DeleteCheckIn`, reload after each write, `checkIn` route flag opens the sheet for the current month once. `AccountDetailViewModelTest` covers: load ok/failed/retry; header "—" for a new account; timeline newest first with "Not checked in"; range filtering 6M/1Y/All; Same as last month; previous-month hint absent on the start month; save success closes the sheet and updates the header; save failure keeps the sheet with "Couldn't save the check-in. Try again."; double tap saves once; delete hidden for start month; delete failure message; Investment shows no check-in. Verify `./gradlew :sharedUI:jvmTest` passes.
- [ ] 6.2 Add `AccountDetailScreen`, `components/` (header, Canvas balance chart with tap/hover label, single-point note and semantics summary, timeline, check-in sheet as bottom sheet / dialog, delete confirmation) and the one-column / two-pane layout. Verify on an Android phone, a tablet emulator and a resized desktop window against every requirement in `specs/finance-account-detail-screen/spec.md`, including TalkBack reading the change with sign and currency.
- [ ] 6.3 Add `AccountDetailNavigation.kt` (route `account/{id}?checkIn={checkIn}`) and wire it in `App` from `homeScreen`. Verify on Android and desktop: card tap opens the detail, back returns with the same card selected, a check-in then back shows the new balance on the card, and "Check in" in Recent activity opens the sheet.

## 7. Account Detail (iOS)

- [ ] 7.1 Add `AccountDetailModel` (`@Observable`) with the same states and rules as 6.1, calling `CheckIn`, `DeleteCheckIn`, `repository.get` and `today()` from `SharedLogic`. Verify the app builds (`xcodebuild` for the simulator) and the model handles a failed load by showing the retry state.
- [ ] 7.2 Add `AccountDetailView`, the Swift Charts balance chart (6M/1Y/All, tap selection, accessibility summary), timeline, `CheckInSheet` and delete confirmation, one column on compact width and two panes on regular width; push it from `HomeView` via `navigationDestination`. Verify on iPhone and iPad against `specs/finance-account-detail-screen/spec.md` and the check-in scenarios in `specs/finance-monthly-tracking/spec.md`, including VoiceOver on the header change.

## 8. Integration and docs

- [x] 8.1 Update `docs/system/overview.md` with a short delta: accounts are tracked by periods and monthly entries, Account Detail exists, Investments start at 0.00 until trades. Verify the paragraph is present and accurate.
- [ ] 8.2 End-to-end check on Android, desktop and iOS: install over a build with saved accounts and confirm the empty state (fresh database); create a Bank account, check in for the current month, restart and confirm the balance and entry persist; create an Investment account and confirm 0.00 and "No trades yet". Verify `./gradlew allTests` (or each module's test task) passes and record the manual results in the PR description.

## Workflow follow-up

- Run `/opsx:archive` after review to merge the delta specs into `openspec/specs/`.
- BRD §4.3 "Already delivered" can then list Monthly tracking and Account Detail.
