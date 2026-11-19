# Proposal

## Why

Once an account is created, the user can't rename it, correct its current balance without opening a check-in, or remove it. BRD J6 (§8.3, §8.4, FR-AD-3) needs both edit and delete, and they are the rest of slice S1. S2 depends on S1, and D25 makes "delete and create a new one" the only way to change an account's tracking mode, so delete has to exist before S2 adds the mode choice.

## What Changes

- **Edit account** (J6, replaces the FR-AC-4 table): an **Edit** action in the Account Detail top bar opens a short sheet titled "Edit account" with a **Save** action. Only two things can be edited:
  - **Name**, with the same rule as Add Account (required, trimmed).
  - **Balance this month**, for Monthly accounts only. Saving a different amount stores the current month's entry, exactly like a check-in for the current month: an existing entry for that month is replaced, and earlier months are untouched. An unchanged amount writes no entry, so a carried-forward month stays "Not checked in".
  - Investment accounts show the name field only.
  - Type, currency, details and tracking mode can't be edited. If one is wrong, the user deletes the account and creates a new one (new BRD decision **D26**).
  - Saving with nothing changed closes the sheet without writing anything.
  - A failed save keeps the sheet open with what the user typed and shows "Couldn't save the changes. Try again."
- **Delete account** (FR-AC-5, FR-AD-3): an overflow menu in the Account Detail top bar holds **Delete account**. It opens the BRD's confirmation dialog. Confirming removes the account and all its entries.
  - Afterwards My Finances selects the nearest neighbouring card: the next one, or the previous one when the deleted card was last. With no accounts left, the empty state is shown.
  - On a phone, Account Detail closes. When two panes fit, the detail pane moves to the neighbour, or to "Open an account to see its history" when none is left.
  - If the delete fails, nothing is removed, Account Detail stays open and "Couldn't delete the account. Try again." is shown.
- **My Finances follows changes made beside it:** when Account Detail is shown beside My Finances, an edit, delete or check-in now updates the cards right away. Today My Finances only reloads when it becomes visible again, which never happens while the two panes share the window.
- **BRD update:** add D26, rewrite FR-AC-4 to match, and update the J6 row in §6.
- The same features on Android, desktop and iOS (NFR-3).

Out of scope: editing the current amount of a Transactions-mode account (S2 decides that), and removing the leftover tracking-period storage (`account_period`, `periodId`), which is a separate cleanup change before S2.

## Capabilities

### New Capabilities
- `finance-account-management`: editing an account's name and current balance, deleting an account with confirmation, the neighbour selected after a delete, and the failure behaviour of both.

### Modified Capabilities
- `finance-account-detail-screen`: the top bar gains Edit and an overflow menu holding Delete account, hidden while the account is loading or failed to load. "Updates after a change" covers account edits, and My Finances updates right away when it is beside Account Detail.
- `finance-accounts-overview`: when the selected account no longer exists after a delete, the neighbouring card is selected instead of the first card.

## Impact

- `:feature:accounts` domain: new `EditAccountUseCase` and `DeleteAccountUseCase`, a shared `neighbourAfterRemoving` rule, and `AccountRepository.edit(...)` and `delete(id)`.
- `:feature:accounts` data: DAO name update, account delete (the existing `ON DELETE CASCADE` removes periods and entries) and a transaction that saves the name and entry together. No schema change.
- `:sharedUI`: the Account Detail top bar, the edit sheet, the delete dialog and snackbar, new Account Detail effects, the selection rule in `HomeViewModel`, and a refresh signal in `App` passed to My Finances.
- `iosApp`: matching SwiftUI changes in `AccountDetailView`/`AccountDetailModel`, `HomeView`/`HomeModel` and `ContentView`, plus `PreviewAccountRepository`.
- Test fakes: both `FakeAccountRepository` classes gain `edit` and `delete`.
- `docs/brd.md`: D26, FR-AC-4 and the J6 row.
