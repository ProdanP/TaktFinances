# Proposal

## Why

Accounts today hold one amount, fixed when they are created, and nothing can update it. The BRD's core promise (§1, goals 2–3) is that a user keeps each account current with a month-end balance and sees how it changes month by month. This change delivers the first part of BRD slice S1: Monthly tracking and an Account Detail screen, so balances can move and history starts to build. It also gives Investment accounts the shape the BRD fixes for them (D16), so later slices don't have to reshape stored data again.

## What Changes

- **BREAKING (data):** The local database is replaced with a new schema: an account no longer stores an amount; its balance comes from a tracking **period** and its **monthly entries**. Accounts saved by development builds are discarded, with no migration (BRD D24).
- Every account records the date it was created. Entries can't be dated before the account's creation month (D17).
- Bank and Loan accounts are created in **Monthly** mode (D3). The Add Account amount is labelled **"Balance this month"** and becomes the entry for the current month. The tracking-mode choice (FR-AC-1) is **not shown yet**: Monthly is the only option until Transactions mode is delivered in S2.
- **BREAKING (behaviour):** Investment accounts have **no amount field**. The form says "Investments are tracked by trades. You'll add your holdings as trades after saving." A new Investment account starts at 0.00 and is tracked by trades (D16). Trades themselves arrive in S2b, so until then an Investment account stays at 0.00 and its activity reads "No trades yet".
- **Monthly check-in** (FR-MO-1): from Account Detail the user enters a month's balance, with the previous month's balance as a hint and a "Same as last month" shortcut. One entry per month; saving a month that has an entry replaces it.
- **Edit and delete monthly entries** (FR-MO-2). The start month can be edited but not deleted.
- **Missing months** carry the previous balance forward and are labelled "Not checked in" (FR-MO-3).
- **Account Detail screen** (§10.1, FR-AD-1, FR-AD-2, FR-MO-4): header with balance and "This month" change, a month-end balance chart (6M / 1Y / All), and the monthly timeline. Shown beside My Finances on wide windows.
- **My Finances**: tapping a card opens Account Detail (FR-MF-4); cards show the current balance; "Recent activity" shows the latest monthly entries with a "See all" action (FR-MF-3).
- Same features on Android, desktop and iOS (NFR-3).

Out of scope, left for later changes: editing and deleting accounts (rest of S1), the tracking-mode choice and Transactions mode (S2), trades (S2b), mode switching and frozen periods (S3), Main Currency equivalents (S4), type-specific blocks (S6).

## Capabilities

### New Capabilities
- `finance-monthly-tracking`: tracking periods, the month-end balance rule, Monthly check-in, editing and deleting monthly entries, carry-forward for missing months, and the creation-date limit for entries.
- `finance-account-detail-screen`: the Account Detail screen: header, balance chart, monthly timeline, activity for accounts tracked by trades, load failure and adaptive layout.

### Modified Capabilities
- `finance-account-creation`: the amount is required only for Bank and Loan and is labelled "Balance this month"; Investment has no amount; saving creates a Monthly account with a current-month entry, or an Investment account at 0.00.
- `finance-account-details`: the Investment scenario no longer enters an amount.
- `finance-accounts-overview`: cards show the current balance, tapping a card opens Account Detail, and "Recent activity" lists real monthly entries.

## Impact

- `:feature:accounts` domain: `FinanceAccount` loses `amountMinor` and gains `createdOn`; new tracking types (mode, period, monthly entry) and month-end balance rules; `AddAccount` changes; new `CheckIn` and `DeleteCheckIn` use cases; `AccountRepository` gains read-by-id and monthly-entry writes.
- `:feature:accounts` data: new Room entities and DAO for periods and monthly entries; `finance_account` drops `amount_minor` and adds `created_on`.
- `:sharedLogic`: `AppDatabase` version 2 with the new entities and destructive fallback; new exported schema `2.json`.
- New dependency: `kotlinx-datetime` in `:feature:accounts` domain (dates and months). `docs/system/stack.md` and `domain.md` are updated to allow it.
- `:sharedUI`: Add Account form changes, new Account Detail screen and check-in sheet, My Finances card tap and Recent activity, a new route in `App`.
- `iosApp`: matching SwiftUI changes, with Swift Charts for the balance chart.
