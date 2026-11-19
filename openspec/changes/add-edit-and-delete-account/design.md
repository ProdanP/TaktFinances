# Design

## Context

See proposal.md for why. Current state that shapes the approach:

- Storage is `finance_account` → `account_period` → `monthly_entry`, and both foreign keys use `ON DELETE CASCADE`. Every account has exactly one active period. `AccountRepository` has `add`, `all`, `get`, `saveMonthlyEntry(periodId, entry)` and `deleteMonthlyEntry(periodId, month)`.
- Account ids grow with creation order, and My Finances orders its cards by id (`selectAll() ... ORDER BY id`).
- `CheckInUseCase` already writes "one entry per month, last save wins" through `saveMonthlyEntry`. `SavedAccount.balanceMinor(today)` gives the current balance, including a carried-forward value.
- Account Detail (`AccountDetailViewModel` + `AccountDetailScreen`) has no effects channel today; its top bar has only a back arrow. The check-in sheet is the pattern for a sheet with a save-in-progress flag and an inline error (`CheckInSheetModel`, `SheetError`).
- My Finances reloads on `ON_START` only (`HomeScreen`, iOS `MyFinancesView.onAppear`). When Account Detail is beside it, My Finances stays started, so it never reloads after a change made in the detail pane. On a phone, Account Detail replaces it, and returning triggers a reload.
- `HomeViewModel.contentFor` (and iOS `HomeModel.content(for:)`) keeps the previous selection while it exists and falls back to the first card.
- iOS builds use cases directly from the repository in each model (`CheckInUseCase(repository:)`).

## Goals / Non-Goals

**Goals:**
- Edit and delete rules live in domain use cases, testable without a database or UI, and shared by Compose and SwiftUI.
- One neighbour rule used everywhere a card has to be chosen after a delete.
- No schema change.

**Non-Goals:**
- Removing `account_period` and `periodId`. `edit` takes a `periodId` like `saveMonthlyEntry` does, and the separate cleanup change removes both.
- An undo for delete.
- Editing type, currency, details or tracking mode.

## Decisions

### D-1 A short edit sheet, not the Add Account form

The edit UI is a sheet with a name field and, for Monthly accounts, a "Balance this month" amount field with the currency shown beside it. It follows the check-in sheet's shape: a bottom sheet on narrow widths and a dialog on wide ones in Compose, and a `.sheet` with a `NavigationStack` and Cancel/Save toolbar on iOS.

- Alternative: reuse the Add Account screen in an edit mode, as FR-AC-4 originally described. Rejected: almost every field on it would be read-only, and its preview, missing-fields hint and type switch would have nothing to do.
- The amount field reuses `filterAmountInput` and `amountInputText`, so it accepts exactly what the check-in field accepts.

### D-2 `EditAccountUseCase` compares against the current balance

```kotlin
sealed interface EditAccountResult {
    data object Saved : EditAccountResult
    data object Unchanged : EditAccountResult   // nothing stored
    data object Invalid : EditAccountResult     // blank name, or no amount on a Monthly account
    data class Failed(val reason: Reason) : EditAccountResult
}

class EditAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: SavedAccount, name: String, amount: String, today: LocalDate): EditAccountResult
}
```

- The name is trimmed and must not be blank.
- For a Monthly account, the amount is parsed with `parseAmountMinor`. An entry is written only when the parsed value differs from `account.balanceMinor(today)`. Its month is `checkInMonths(period, today).first()`, the current month, or the start month if the clock reads an earlier date. That way the D17 creation-date limit holds without a second check.
- For an Investment account, `amount` is ignored.
- With the name unchanged and no entry to write, the result is `Unchanged` and the repository is not called.
- Why compare against the balance rather than against the stored entry? The field is filled with the balance. Comparing against the stored entry would treat an untouched carried-forward month as changed (no entry ≠ 2,500.00), and Save would quietly turn it into a check-in.

### D-3 One repository call for name and entry, in one transaction

```kotlin
/** Renames account [id] and, when [entry] is not null, stores it in the Monthly period [periodId], all or nothing. */
suspend fun edit(id: Long, name: String, periodId: Long, entry: MonthlyEntry?): Outcome<Unit>

/** Removes account [id] with its periods and entries. */
suspend fun delete(id: Long): Outcome<Unit>
```

The DAO gets `@Query("UPDATE finance_account SET name = :name WHERE id = :id")`, a `@Transaction` method that runs the update and, if present, `upsertEntry`, and `@Query("DELETE FROM finance_account WHERE id = :id")`. The cascade removes the period and its entries. Both are wrapped in `outcomeOf`.

- Alternative: the use case calls a rename and then `saveMonthlyEntry`. Rejected: if the second call fails, the name has changed but the balance hasn't, which breaks "all or nothing" in the spec.
- If no account has that id, both calls are no-ops. That case can't occur from the UI, so it gets no separate `Reason`.

### D-4 `DeleteAccountUseCase` returns the neighbour, with a shared rule

```kotlin
/** The card to select once [removedId] is gone: the first account after it, else the last one before it. */
fun neighbourAfterRemoving(remaining: List<SavedAccount>, removedId: Long): SavedAccount?

sealed interface DeleteAccountResult {
    data class Deleted(val neighbourId: Long?) : DeleteAccountResult
    data class Failed(val reason: Reason) : DeleteAccountResult
}
```

Because ids grow in card order, the neighbour can be found from the remaining accounts alone: the smallest id greater than the removed one, or else the largest smaller one. The use case deletes, then reads `all()` and applies the rule. If that read fails, the delete still counts as done, with `neighbourId = null`. The detail pane then shows the placeholder, and My Finances shows its own load failure.

`HomeViewModel.contentFor` and iOS `HomeModel.content(for:)` use the same rule: when the previous selection is no longer in the list, they select `neighbourAfterRemoving(list, previousSelection)`, and the first card only when there was no selection. So a phone, which only reloads My Finances on return, lands on the same card the use case picked.

- Alternative: pass the neighbour id from Account Detail through navigation to My Finances. Rejected: it adds a navigation argument, and My Finances can work out the same answer on its own.

### D-5 Account Detail reports changes, and the app shell refreshes My Finances

`AccountDetailViewModel` gains an effects channel, as `HomeViewModel` has:

```kotlin
internal sealed interface AccountDetailEffect {
    data object AccountChanged : AccountDetailEffect                 // edit saved, check-in saved or deleted
    data class AccountDeleted(val neighbourId: Long?) : AccountDetailEffect
    data object DeleteFailed : AccountDetailEffect                   // shown as a snackbar
}
```

`accountDetailEntry` takes `onAccountChanged` and `onAccountDeleted(neighbourId)`. `App` keeps an `accountsVersion` counter (saveable) and passes it to `homeEntry` → `HomeScreen`, which reloads when it changes (not on first composition, where `ON_START` already loads).

- `onAccountChanged` increments the counter.
- `onAccountDeleted` increments the counter, removes every `AccountDetailRoute` and, when two panes fit and there is a neighbour, adds `AccountDetailRoute(neighbourId)`. On a phone, removing the route shows My Finances, which reloads on `ON_START`.

On iOS, `AccountDetailView` takes `onChanged` and `onDeleted(neighbourId)` closures:
- `ContentView` (compact): `onDeleted` pops the path. `MyFinancesView.onAppear` reloads.
- `HomeView` (regular split): `onChanged` sends `.shown` to `HomeModel`. `onDeleted` sends `.shown` and sets `opened` to the neighbour, or `nil` for the placeholder.

Firing `AccountChanged` on check-in saves and deletes too fixes the existing bug where a card next to Account Detail kept its old balance until restart. That is the spec change to "Updates after a change".

- Alternative: have My Finances observe the database (a Room `Flow`). Rejected for now: `AccountRepository` is suspend-only on purpose, so Swift can call it, and a flow would change every caller and both fakes.

### D-6 Failures: inline for edit, snackbar for delete

- Edit failure keeps the sheet open with an inline "Couldn't save the changes. Try again.", like a failed check-in.
- Delete failure happens after the confirmation dialog has closed, so there is no surface left to put an inline message on. Compose shows a snackbar ("Couldn't delete the account. Try again.") from the `DeleteFailed` effect. iOS shows an alert with the same text.
- While a delete is running, `deleting = true` in the state disables Edit and the overflow menu.

### D-7 Top bar actions

The Compose top bar adds an `IconButton` with an edit icon (content description "Edit"), and an overflow `IconButton` ("More options") that opens a `DropdownMenu` holding "Delete account". The iOS toolbar adds an "Edit" button and a `Menu` with a destructive "Delete account" button. They are shown only for `AccountDetailContent.Loaded`. The confirmation dialog is an `AlertDialog` with the error colour on "Delete" in Compose, and `.confirmationDialog`/`.alert` with `role: .destructive` on iOS.

## Risks / Trade-offs

- [The `accountsVersion` counter reloads My Finances even after a change that didn't touch the cards, such as a check-in on an older month.] → One `all()` read, the same cost as returning to the screen. Acceptable.
- [The neighbour rule relies on ids growing with card order.] → `SavedAccount` documents it, and the card order is defined by id. A test in `AccountSummaryTest`'s style covers the rule. If card order ever changes (for example FR-MF-5 "repaid loans sort last" in S6), the rule moves to that order in the same change.
- [`edit` keeps the `periodId` parameter that the period cleanup will remove.] → Recorded in the proposal's out-of-scope list. The cleanup changes one more signature.
- [Deleting cannot be undone.] → The confirmation dialog says so, as the BRD asks.

## Migration Plan

No schema or data migration. The BRD is updated in the same change (D26, FR-AC-4, J6).
