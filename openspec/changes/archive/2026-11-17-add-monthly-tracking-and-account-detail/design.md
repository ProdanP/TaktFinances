# Design

## Context

See proposal.md for why. Current state that shapes the approach:

- `:feature:accounts` stores one row per account in `finance_account`, including `amount_minor`. `FinanceAccount` carries `amountMinor`, and `AccountRepository` has only `add` and `all`.
- `:sharedLogic` owns `AppDatabase` (Room 3, version 1, schema exported to `sharedLogic/schemas/.../1.json`) and lists every feature's entities.
- `domain` depends only on the Kotlin standard library and `:core` (`docs/system/domain.md`). There is no date library in the project; Kotlin is 2.4.20, so `kotlin.time.Clock` and `Instant` are in the standard library, but calendar dates and months are not.
- Android and desktop share one MVI ViewModel per screen in `:sharedUI`; iOS has an `@Observable` model per screen (`docs/system/presentation.md`). There is no chart library.
- Home reloads accounts on every `ON_START`, which is how it picks up a newly added account.

## Goals / Non-Goals

**Goals:**
- A storage shape that later slices extend without reshaping: S2 adds transactions, S2b trades, S3 more periods per account.
- Keep balance and month-end rules in `domain` as pure functions, testable without a database or UI.
- Same behaviour on Android, desktop and iOS.

**Non-Goals:**
- Frozen periods and mode switching. The schema allows several periods per account, but this change always reads and writes exactly one, the active one.
- Transactions and trades tables. They arrive with S2 and S2b.
- Exchange rates and Main Currency equivalents.

## Decisions

### D-1 Add `kotlinx-datetime` to domain

Domain needs `LocalDate` (creation date, period start) and a month value (`YearMonth`), plus "today" in the device's time zone. `kotlinx-datetime` 0.8.x provides `LocalDate`, `YearMonth` and `Clock.System.todayIn(TimeZone.currentSystemDefault())` on every target.

- Alternative: hand-written `YearMonth(year, month)` and epoch-day `Long` dates. Rejected: converting "now" to a local date needs time-zone data, which the standard library doesn't have, so we would need the library anyway or a platform port per target.
- `stack.md` gains the library; `domain.md` changes "standard library and `:core`" to also allow `kotlinx-datetime`. It is exported to Swift through `SharedLogic` (types appear as `Kotlinx_datetimeLocalDate` / `Kotlinx_datetimeYearMonth`), so Swift never builds dates itself: domain provides the labels and month lists it needs.

### D-2 "Today" is a parameter, not a hidden clock

Use cases and balance functions take `today: LocalDate` as an argument. State holders take a `today: () -> LocalDate` constructor argument whose default is a domain top-level function `today()` (`Clock.System.todayIn(TimeZone.currentSystemDefault())`). Swift models call the same `today()` function.

- Why: every rule that depends on the date ("current month", months offered, creation date) is testable with a fixed date and needs no fake clock type.
- Alternative: a `Clock` port bound in Koin. Rejected: one implementation, and screens would need a new dependency threaded from `AppDependencies` for no gain.

### D-3 Domain model

```kotlin
enum class TrackingMode { Monthly, Transactions }

data class MonthlyEntry(val month: YearMonth, val amountMinor: Long)

/** The account's active tracking period. */
sealed interface Period {
    val id: Long
    val start: LocalDate
    data class Monthly(override val id: Long, override val start: LocalDate,
                       val entries: List<MonthlyEntry>) : Period      // sorted by month, start month first
    data class Transactions(override val id: Long, override val start: LocalDate,
                            val openingBalanceMinor: Long) : Period
}

data class FinanceAccount(name, currency, details, createdOn: LocalDate)   // amountMinor removed
data class SavedAccount(val id: Long, val account: FinanceAccount, val period: Period)
```

Pure functions next to these types in `domain.entity`:

- `monthEndBalances(account: SavedAccount, today): List<MonthBalance>` with `MonthBalance(month, balanceMinor, checkedIn: Boolean)`, from the first month to the current month, oldest first. Monthly: entry or carry-forward. Transactions: `openingBalanceMinor` for every month (no transactions or trades exist yet; S2/S2b extend this function).
- `SavedAccount.balanceMinor(today)` = last element of the above.
- `monthChange(previous: Long?, current: Long): MonthChange?` with `MonthChange(amountMinor, percentTenths: Long?)` (percent kept as tenths of a percent so it is exact and formats as "15.0%"); null for the first month, `percentTenths` null when previous is 0.
- `checkInMonths(period: Period.Monthly, today): List<YearMonth>` newest first.
- Formatting: `formatSignedAmount(minor)` ("+1,500.00", "−500.00" with U+2212, "0.00"), `formatPercent(tenths)`, `monthLabel(month)` ("November 2026"), `shortMonthLabel(month)` ("October"). `formatAmount` stays as is for non-negative values.

`SavedAccount` carries its active period (with entries) rather than a separate "history" read, because both My Finances (balance, recent activity) and Account Detail need it, and the volume is small (BRD A1: a few dozen accounts, one entry per month).

- Alternative: keep `balanceMinor` as a stored column updated on each write. Rejected: two sources of truth for the same number; the rule would live in SQL updates instead of domain.

`TrackingMode` is in the model now even though Add Account has no choice yet, because the period type is decided by it and S2 only has to expose the choice.

### D-4 Storage schema and fresh database

```
finance_account   id PK, type, name, currency, created_on TEXT (ISO date), bank, bank_kind, last4,
                  investment_kind, platform, loan_direction, counterparty          -- amount_minor dropped
account_period    id PK, account_id FK→finance_account ON DELETE CASCADE (indexed),
                  mode TrackingModeDbo, start_date TEXT, end_date TEXT NULL,
                  opening_balance_minor INTEGER NULL                               -- Transactions only
monthly_entry     period_id FK→account_period ON DELETE CASCADE, month TEXT ('YYYY-MM'),
                  amount_minor INTEGER, PRIMARY KEY (period_id, month)
```

- Dates are ISO text (`2026-11-16`, `2026-11`): sortable, readable in the database inspector, and mapped in `data.mapper` with `LocalDate.parse` / `YearMonth.parse`, so no Room `TypeConverter` is needed. A malformed value throws in `toDomain()` and becomes `Outcome.Failed`, like other unreadable rows.
- A Monthly period's starting balance is its start month's entry, so `opening_balance_minor` is null for Monthly periods. This avoids a second copy that editing the start month would have to keep in sync.
- `end_date` exists for S3 and is always null here; the active period is the one with `end_date IS NULL`.
- `AppDatabase` goes to version 2 with `fallbackToDestructiveMigration(dropAllTables = true)` on the builder in `buildApp()` (BRD D24: development data is discarded). The exported `2.json` is committed. Once the app is released this fallback must be removed and real migrations used; the builder comment says so.
- Alternative: keep version 1 and edit the schema. Rejected: existing dev installs would crash on the identity hash mismatch.

### D-5 Repository

```kotlin
interface AccountRepository {
    suspend fun add(account: FinanceAccount, start: StartingPoint): Outcome<SavedAccount>
    suspend fun all(): Outcome<List<SavedAccount>>                // oldest first, with active period
    suspend fun get(id: Long): Outcome<SavedAccount>
    suspend fun saveMonthlyEntry(periodId: Long, entry: MonthlyEntry): Outcome<Unit>   // upsert
    suspend fun deleteMonthlyEntry(periodId: Long, month: YearMonth): Outcome<Unit>
}

sealed interface StartingPoint {
    data class Monthly(val entry: MonthlyEntry) : StartingPoint
    data class Transactions(val openingBalanceMinor: Long) : StartingPoint
}
```

- `add` inserts the account, its period and (for Monthly) the first entry in one `@Transaction` DAO function, per `data-access.md` "Writes".
- `all` and `get` read accounts, active periods and entries with three queries (accounts; active periods; entries of active periods) and join them in the mapper, instead of a Room `@Relation`, so the active-period filter stays explicit and S3 can add frozen periods without changing the relation shape.
- A missing account in `get` is `Reason.Unavailable` (Account Detail shows the load failure); no feature-specific reason is needed yet.
- Accounts, periods and entries are one aggregate, so `FinanceAccountDao` holds the queries for all three tables, including the `@Transaction` `insertWithStart`. No second DAO is added, so `:sharedLogic`'s database module needs no new binding. (Chosen during implementation over a separate `TrackingDao`, which would have split the transactional insert across two DAOs.)

### D-6 Use cases

- `AddAccount(repository)` — `invoke(type, name, currency, amount, details, today)`. Bank/Loan → `StartingPoint.Monthly(MonthlyEntry(YearMonth(today), amount))`; Investment → `StartingPoint.Transactions(0)` and the amount is ignored. `checkAccountForm` skips the amount for Investment.
- `CheckIn(repository)` — `invoke(account: SavedAccount, month, amount: String, today): CheckInResult` (`Saved` / `Invalid` / `Failed(reason)`). Rules: the account is Monthly, `month` is in `checkInMonths`, the amount parses. Invalid input never reaches the repository.
- `DeleteCheckIn(repository)` — `invoke(account, month): DeleteCheckInResult`. Rule: never the start month; the month must have an entry.

Reading an account by id has no rule, so the state holder calls `repository.get` directly (KISS rule in `architecture.md`).

### D-7 Presentation

Android/desktop (`:sharedUI`, package `com.prodan.taktfinances.accounts`):

- New screen set `AccountDetailScreen/State/Intent/ViewModel/Effect`. State holds `content: Loading | Failed | Loaded(account, monthBalances, timeline rows, header model)`, `range: ChartRange` (`SixMonths` default, `OneYear`, `All`), and `sheet: CheckInSheet?` (month, amount text, focused, saving, failed, confirmDelete). UI models (`AccountDetailHeaderModel`, `TimelineRowModel`, `ChartPointModel`) live in `models/` with `toUi()` mappers that use the domain formatters.
- The check-in sheet is part of the Account Detail screen (a `ModalBottomSheet` on compact widths, a dialog on wide windows), not its own route, so its state lives in the same ViewModel and saving reloads the same screen.
- Navigation 3 key `AccountDetailRoute(id: Long, checkIn: Boolean = false)` in `accounts/navigation/AccountDetailNavigation.kt`, registered in `App`'s saved-state serializers. `checkIn = true` opens the sheet for the current month on first load (My Finances "Check in" action). `App` wires `homeEntry(onNavigateToAccount = { id, checkIn -> ... })`, which removes any `AccountDetailRoute` from the back stack before adding the new one, so a second account replaces the first. The ViewModel is keyed by account id.
- Chart: drawn with Compose `Canvas` (line, points, selected-point label; pointer hover on desktop via `onPointerEvent`), with `semantics { contentDescription = summary }`. No chart library is added for one line chart.
- Layout uses Material 3 adaptive's `ListDetailSceneStrategy` in `App`'s `NavDisplay`: the My Finances `HomeRoute` entry is `listPane(detailPlaceholder = { AccountDetailPlaceholder() })` and the Account Detail entry is `detailPane()`. When the window's pane directive allows two partitions they show side by side; otherwise only the top entry shows. `App` passes `showBack = !twoPane`, so the back arrow appears only when Account Detail is alone. Inside its pane, Account Detail splits header and chart beside action and timeline when `widthClass` of its own `BoxWithConstraints` is `Wide`, and the check-in form is a dialog in that case and a bottom sheet otherwise.
  - Chosen during implementation over a self-contained two-pane Account Detail: My Finances stays visible while the user goes through accounts, and Nav3 handles collapsing to one pane and back.
- Home: `HomeIntent.AccountTapped(id)`, `HomeIntent.CheckInTapped(id)`, `HomeIntent.SeeAllTapped(id)` → `HomeEffect.OpenAccount(id, checkIn)`. Since Home already reloads on `ON_START`, balances refresh after returning from Account Detail with no extra wiring. `RecentActivity` gets a model built from the selected account's entries.
- Add Account: amount field hidden for Investment, with the notice; label "Balance this month". `AddAccountViewModel` passes `today()` to `AddAccount`. The preview shows `0.00` for Investment.

iOS (`iosApp/iosApp/Accounts/`): `AccountDetailView` + `AccountDetailModel` (`@Observable`), `CheckInSheet` view, chart with Swift Charts (system framework; `stack.md` notes it). The My Finances tab uses a `NavigationSplitView` with My Finances as the sidebar and Account Detail (or the "Open an account to see its history" placeholder) as the detail, bound to the selected account id in `HomeModel`; on compact width it collapses to a stack that pushes Account Detail, matching the Android/desktop list-detail behaviour. Inside the detail, header and chart sit beside action and timeline only when the detail's own width is wide (`ViewThatFits` or a `GeometryReader` on the same 840pt threshold), otherwise one column.

### D-8 Investment accounts without trades

An Investment account gets a `Transactions` period with `openingBalanceMinor = 0`. Account Detail and Recent activity branch on the period type: `Monthly` → check-in UI and monthly timeline; `Transactions` → "No trades yet". The chart still draws (flat at 0.00), which keeps the screen uniform and is what S2b fills in.

## Risks / Trade-offs

- [Swift sees `kotlinx-datetime` types with awkward names] → Domain exposes label and list functions (`monthLabel`, `checkInMonths`) so Swift only passes values through; `YearMonth` is compared by calling Kotlin `equals`.
- [Destructive fallback could ship to users by mistake] → Comment on the builder and a task to note it in `docs/learnings.md`; a later release-prep change replaces it with migrations.
- [Loading all entries for all accounts on My Finances] → Fine for BRD A1 volumes (dozens of accounts × one row per month); revisit with a query for the latest entries if NFR-5 measurements say otherwise.
- [Day boundary: the app stays open across midnight on the last day of a month] → "Today" is read on each load and each check-in, so the next reload uses the new month; no timer is added.
- [Custom Canvas chart is more code than a library] → One simple line chart; a library would add a dependency to every target for this one use.

## Migration Plan

No user data exists (the app is unreleased). Installing this build drops the version 1 tables and creates version 2 (D-4). Rollback is reinstalling the previous build, which would in turn drop the version 2 tables only if it had a destructive fallback; it does not, so a rollback needs an uninstall. Acceptable for development builds.
