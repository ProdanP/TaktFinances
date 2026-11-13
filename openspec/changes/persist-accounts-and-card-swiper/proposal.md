# Proposal

## Why

Accounts created on the Add Account screen live only in memory: they vanish when the app restarts and nothing shows them, since My Finances is still a placeholder. Storing them locally and showing them on My Finances makes the Add Account flow useful end to end.

## What Changes

- Accounts are saved to the app's local SQLite database (SQLDelight) and are still there after the app restarts.
- Saving can fail; the Add Account screen then keeps the entries and says the account could not be saved.
- My Finances replaces its placeholder with a horizontal card swiper of the saved accounts, oldest first, with neighbouring cards peeking at the edges and a page indicator.
- The selected card drives a "Recent activity" area below the swiper, which for now shows a "No transactions yet" empty state.
- With no accounts, My Finances shows a single "Add your first account" card that opens Add Account.
- After a successful save, My Finances shows the new account's card selected.
- If the accounts cannot be read, My Finances says so and offers a retry.
- On wide windows (tablet landscape, desktop) My Finances shows two panes — the swiper beside the "Recent activity" area — and offers previous/next buttons and arrow keys for pointer and keyboard users.
- Add Account adapts to the window: one column on phones, a bounded centred column on medium widths, and on wide windows the live preview stays in view beside a scrolling form.
- Infrastructure: SQLDelight is added, a `:core` module holds `Outcome`, the database driver is created in each app shell, and `:sharedLogic` opens the database and wires dependencies with Koin.

Out of scope: editing or deleting accounts, transactions, a total across currencies, and migrating in-memory data (there is none to keep).

## Capabilities

### New Capabilities
- `finance-accounts-overview`: How My Finances presents the user's saved accounts — the card swiper, selection, the area driven by the selected card, the empty state, load failure, and accounts persisting across restarts.

### Modified Capabilities
- `finance-account-creation`: "Create an account" now saves the account locally, lands on the new account's card, and handles a failed save; the Add Account screen adapts its layout to phone, tablet and desktop windows.

## Impact

- `feature/accounts`: repository becomes SQLDelight-backed and `suspend`, returns `Outcome`; new `.sq` file; `AddAccount` becomes `suspend` and gains a failed result; factory takes the database driver.
- New `:core` module with `Outcome` / `Reason`.
- `:sharedLogic`: Koin modules (the composition root), opens the single database, exposes platform driver helpers and an `AppDependencies` entry point for Compose and Swift.
- `:sharedUI`: `HomeViewModel` loads accounts; My Finances swiper, activity area and wide two-pane layout; Add Account adaptive layouts; a display-only account card shared with the Add Account preview; `App` receives its dependencies instead of creating the repository.
- `androidApp`, `desktopApp`, `iosApp`: create the database driver and start Koin at startup.
- iOS: `HomeModel`/`HomeView` load and show accounts with iPhone and iPad layouts; `AddAccountView` gains the iPad layout; `AddAccountModel` awaits the save.
- Dependencies: SQLDelight plugin, runtime, Android / native / JDBC drivers, `koin-core` and `koin-test` added to `docs/system/stack.md` (Koin is already allowed there) and the version catalog.
