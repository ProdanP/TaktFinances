# Proposal

## Why

The My Finances tab is an empty placeholder and the app has no way to record a finance account. Adding accounts is the first step toward tracking money, so users need an entry point and a minimal form to create one.

## What Changes

- My Finances tab gets a top bar with a "+" action (Android and desktop: Compose top app bar; iOS: navigation bar toolbar button).
- Tapping "+" opens a new Add Account screen, pushed above the Home shell (Android/desktop via Navigation Compose route; iOS via `NavigationStack`).
- The Add Account form collects the minimum: account type (Bank Account, Investment, Loan), a name, a currency, and a current amount.
- Saving validates input, creates the account through a repository, and returns to My Finances. Cancel/back returns without saving.
- New `:feature:accounts` module holding the account domain (type, currency, model, repository interface, add-account use case) and an in-memory repository.

Out of scope (assumptions): showing the created accounts in a list on My Finances, editing/deleting accounts, cross-currency conversion or totals, and durable storage (SQLDelight). Accounts live in memory for the app session; persistence and the list are follow-up changes.

## Capabilities

### New Capabilities
- `finance-account-creation`: Entry point from My Finances and the Add Account form that creates a Bank Account, Investment, or Loan with a name, currency, and amount.

### Modified Capabilities

## Impact

- New module `feature/accounts` (registered in `settings.gradle.kts`, `api` dependency and iOS `export` in `:sharedLogic`, dependency from `:sharedUI`).
- `sharedUI`: `App.kt` route, `HomeScreen` My Finances top bar, new `accounts` package (screen, state, intent, effect, ViewModel).
- `iosApp`: `HomeView` toolbar, new `Accounts/` SwiftUI form and `@Observable` model.
- No new third-party libraries.
