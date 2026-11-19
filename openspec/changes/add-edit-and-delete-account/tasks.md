# Tasks

## 1. BRD

- [x] 1.1 In `docs/brd.md`: add **D26** to the decision log ("Only an account's name and current balance can be edited. Type, currency, details and tracking mode are fixed at creation; to change one, the user deletes the account and creates a new one."). Rewrite §8.3 FR-AC-4 to match `specs/finance-account-management/spec.md` (Edit sheet titled "Edit account", name and "Balance this month" for Monthly accounts, name only for Investment, unchanged amount writes no entry, Transactions-mode amount left to S2). Change the J6 row in §6 to "Edits the name or this month's balance and saves, or deletes after confirming". Verify a search of `docs/brd.md` finds no remaining "Can't be changed once the account has history" or "Always" editable-details row.

## 2. Domain

- [x] 2.1 Add `edit(id, name, periodId, entry)` and `delete(id)` to `AccountRepository` (design D-3) and stub them in both `FakeAccountRepository` classes (`feature/accounts` and `sharedUI` `commonTest`), recording calls and returning a configurable outcome. Verify `./gradlew :feature:accounts:compileTestKotlinJvm :sharedUI:compileTestKotlinJvm` succeeds.
- [x] 2.2 Add `neighbourAfterRemoving(remaining, removedId)` (design D-4) with `commonTest` coverage: middle id → next, last id → previous, first id → next, only account → null. Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.3 Add `EditAccountUseCase` and `EditAccountResult` (design D-2) with `commonTest` coverage: blank name → `Invalid` with no repository call; trimmed rename only → `edit` with `entry = null`; amount different from a carried-forward balance → entry for the current month; amount equal to the carried-forward balance and the name unchanged → `Unchanged` with no repository call; amount equal to an existing current-month entry → no entry; Monthly with an unparsable amount → `Invalid`; Investment ignores the amount; storage failure → `Failed`. Verify `./gradlew :feature:accounts:jvmTest` passes.
- [x] 2.4 Add `DeleteAccountUseCase` and `DeleteAccountResult` (design D-4) with tests: success returns the neighbour from the accounts read after the delete; the read failing after a successful delete returns `Deleted(null)`; a delete failure returns `Failed` and does not read. Register both use cases in `AccountsModule`. Verify `./gradlew :feature:accounts:jvmTest` passes and `PresentationModuleTest` still resolves the graph.

## 3. Data

- [x] 3.1 Add to `FinanceAccountDao` the name update, a `@Transaction` method that updates the name and upserts an optional entry, and the account delete; implement `AccountRepositoryImpl.edit` and `delete` with `outcomeOf` (design D-3). Verify `./gradlew :feature:accounts:compileKotlinJvm` succeeds.
- [x] 3.2 Extend `AccountRepositoryTest` (jvmTest, in-memory Room): `edit` renames and, with an entry, inserts or replaces the current month's entry; `edit` with `entry = null` leaves entries unchanged; `delete` removes the account, its period and its entries (the cascade), and `all` no longer returns it; both return `Failed` on a closed database. Verify `./gradlew :feature:accounts:jvmTest` passes.

## 4. Account Detail (Compose)

- [x] 4.1 Add `AccountDetailEffect` (design D-5) and an effects channel to `AccountDetailViewModel`; send `AccountChanged` after a check-in save or delete succeeds. Extend `AccountDetailViewModelTest` to check that both send it. Verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 4.2 Edit flow: an `EditSheetModel` in `models/` (name, amount, `showsAmount`, `canSave`, `saving`, `error`), intents for Edit tapped, name and amount changed, save and dismiss; `save` calls `EditAccountUseCase`. `Saved` closes the sheet, reloads and sends `AccountChanged`; `Unchanged` closes the sheet; `Failed` keeps it open with "Couldn't save the changes. Try again."; a save in progress ignores a second Save. Tests cover each scenario in `specs/finance-account-management/spec.md` from "Open Edit account" to "No double edit save". Verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 4.3 Delete flow: intents for Delete account chosen, confirmed and cancelled; state `confirmDeleteAccount` and `deleting`; confirming calls `DeleteAccountUseCase`; `Deleted(n)` sends `AccountDeleted(n)`; `Failed` clears `deleting` and sends `DeleteFailed`; while `deleting`, Edit and Delete do nothing. Tests cover the "Delete account confirmation", "Failed delete keeps the account" and "No double delete" scenarios. Verify `./gradlew :sharedUI:jvmTest` passes.
- [x] 4.4 Screen: the top bar shows Edit and the "More options" menu with "Delete account" only when loaded (design D-7); add an `EditAccountSheet` component in the check-in sheet's narrow and wide shapes; the confirmation `AlertDialog` with the BRD text and a destructive "Delete"; a `SnackbarHost` for `DeleteFailed`. Extend `AccountDetailScreenTest` (jvmTest): actions hidden while loading and after a failed load, the menu shows "Delete account", the dialog text, and the sheet hides the amount for Investment. Verify `./gradlew :sharedUI:jvmTest` passes.

## 5. My Finances and navigation (Compose)

- [x] 5.1 `HomeViewModel.contentFor`: when the previous selection is gone, select `neighbourAfterRemoving(list, previousSelection)`, and the first card only when there was no previous selection. Extend `HomeViewModelTest` with the "Selected account deleted" scenario and the last-card case. Verify `./gradlew :sharedUI:jvmTest` passes.
- [ ] 5.2 `accountDetailEntry` takes `onAccountChanged` and `onAccountDeleted(neighbourId)`, and `AccountDetailScreen` collects the effects. `App` keeps a saveable `accountsVersion`, passes it through `homeEntry` to `HomeScreen`, which reloads when it changes but not on first composition. On delete, `App` removes the detail route and, when two panes fit and there is a neighbour, adds `AccountDetailRoute(neighbourId)` (design D-5). Verify `./gradlew :sharedUI:jvmTest` passes and, on desktop (`./gradlew :desktopApp:run`) in a wide window: delete a middle account and the detail pane moves to the next one; delete the last remaining account and the placeholder shows; a check-in or edit updates the card beside it at once.
- [ ] 5.3 On an Android phone or emulator: edit the name and balance of an account and see it on Account Detail and after going back; delete it and land on My Finances with the neighbour selected; restart and confirm it stays deleted.

## 6. iOS

- [x] 6.1 `AccountDetailModel`/`AccountDetailView`: the Edit toolbar button and a `Menu` with a destructive "Delete account", shown only when loaded; the edit `.sheet` (name, and "Balance this month" for Monthly) using `EditAccountUseCase`; the confirmation dialog with the BRD text using `DeleteAccountUseCase`; an alert for a failed delete; `onChanged` and `onDeleted(neighbourId)` closures, with `onChanged` also called after check-in saves and deletes. Add `edit` and `delete` to `PreviewAccountRepository`. Verify the app builds for the simulator.
- [ ] 6.2 `HomeModel.content(for:)` uses `neighbourAfterRemoving` when the previous selection is gone. `ContentView` pops the path on delete. `HomeView` (regular split) reloads on `onChanged` and on `onDeleted` sets `opened` to the neighbour or `nil`. Verify in the iOS simulator, on an iPhone: edit, delete, and land on the neighbour; on an iPad in landscape: delete moves the detail column to the neighbour, and a check-in updates the card beside it.

## 7. Integration

- [ ] 7.1 Run `./gradlew :feature:accounts:jvmTest :sharedLogic:jvmTest :sharedUI:jvmTest` and `openspec validate add-edit-and-delete-account --strict`, and check every scenario in the three spec deltas has a test or a manual check from the tasks above. Verify all pass.

## Workflow follow-up

- Archive the change after review, then sync the delta specs into `openspec/specs/`.
- Next: the period storage cleanup change (move `mode` and opening balance onto `finance_account`, drop `account_period` and the `periodId` parameters) before S2.
