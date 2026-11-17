# Spec Delta

## Purpose

Defines how an account's balance is kept up to date over time: its tracking period, the monthly entries of Monthly-mode accounts, how a month-end balance is derived for every month, and how the user checks in, edits and deletes monthly entries.

## ADDED Requirements

### Requirement: Account creation date
Every account SHALL record the calendar date on which it was created, in the device's local time zone. That date SHALL NOT change afterwards.

#### Scenario: Created today
- **WHEN** the user creates an account on 16 Nov 2026
- **THEN** the account's creation date is 16 Nov 2026 and its first month is November 2026

### Requirement: Tracking mode by account type
Bank and Loan accounts SHALL be tracked in Monthly mode. Investment accounts SHALL always be tracked by trades and SHALL have no Monthly mode.

#### Scenario: New Bank account
- **WHEN** the user creates a Bank account
- **THEN** the account is tracked in Monthly mode

#### Scenario: New Loan account
- **WHEN** the user creates a Loan account
- **THEN** the account is tracked in Monthly mode

#### Scenario: New Investment account
- **WHEN** the user creates an Investment account
- **THEN** the account is tracked by trades and has no monthly entries

### Requirement: Starting point of a Monthly account
Creating a Monthly account SHALL create one monthly entry for the creation month holding the amount the user entered. That month is the account's start month.

#### Scenario: Monthly account created
- **WHEN** the user creates a Bank account with 2,500.00 MDL in November 2026
- **THEN** the account has exactly one monthly entry, November 2026: 2,500.00, and its balance is 2,500.00

### Requirement: Starting point of an Investment account
A new Investment account SHALL start with a value of 0.00 dated on its creation date, and SHALL have no holdings and no trades.

#### Scenario: Investment account created
- **WHEN** the user creates an Investment account in EUR on 16 Nov 2026
- **THEN** its balance is 0.00 EUR and it has no trades

### Requirement: One monthly entry per month
A Monthly account SHALL hold at most one entry per calendar month. Saving an entry for a month that already has one SHALL replace it.

#### Scenario: Replace an entry
- **WHEN** the account has November 2026: 11,500.00 and the user saves November 2026 with 12,000.00
- **THEN** the account has one November 2026 entry, holding 12,000.00

### Requirement: Month-end balance
For each month from an account's first month through the current month, the system SHALL derive a month-end balance. For a Monthly account it SHALL be that month's entry, or the latest earlier entry when the month has none. For an Investment account it SHALL be 0.00 until trades exist.

#### Scenario: Month with an entry
- **WHEN** a Monthly account has October 2026: 10,000.00 and November 2026: 11,500.00
- **THEN** the month-end balance for November 2026 is 11,500.00

#### Scenario: Missing month carries forward
- **WHEN** a Monthly account has September 2026: 9,000.00 and November 2026: 11,500.00 and no October entry
- **THEN** the month-end balance for October 2026 is 9,000.00

#### Scenario: Missing month is never zero
- **WHEN** the latest entry is October 2026: 10,000.00 and it is now November 2026 with no November entry
- **THEN** the month-end balance for November 2026 is 10,000.00, not 0.00

#### Scenario: Months before the account
- **WHEN** an account was created in November 2026
- **THEN** it has no month-end balance for October 2026 or earlier

### Requirement: Current balance
An account's current balance SHALL be its month-end balance for the current month.

#### Scenario: Balance after a check-in
- **WHEN** a Monthly account's latest entry is November 2026: 11,500.00 and it is November 2026
- **THEN** its balance is 11,500.00

### Requirement: Monthly change
The change for a month SHALL be its month-end balance minus the previous month's month-end balance, shown as an amount and a percentage of the previous balance. The percentage SHALL be hidden when the previous balance is 0.00. The first month of an account SHALL have no change.

#### Scenario: Increase
- **WHEN** October 2026 is 10,000.00 and November 2026 is 11,500.00
- **THEN** the change for November is +1,500.00 (+15.0%)

#### Scenario: Decrease
- **WHEN** October 2026 is 10,000.00 and November 2026 is 9,500.00
- **THEN** the change for November is −500.00 (−5.0%)

#### Scenario: No change
- **WHEN** October 2026 and November 2026 are both 10,000.00
- **THEN** the change for November is 0.00 (0.0%), shown as neutral

#### Scenario: Previous balance is zero
- **WHEN** October 2026 is 0.00 and November 2026 is 500.00
- **THEN** the change for November is +500.00 with no percentage

### Requirement: Check in for a month
Account Detail of a Monthly account SHALL offer a "Check in" action that opens a sheet with a month choice, a balance amount, a hint with the previous month's balance, and a "Same as last month" shortcut. The current month SHALL be selected when the sheet opens from "Check in".

#### Scenario: Check in for the current month
- **WHEN** the account's latest entry is October 2026: 10,000.00 and in November 2026 the user taps "Check in", enters 11,500.00 and saves
- **THEN** November 2026: 11,500.00 is saved, the sheet closes, the balance shows 11,500.00 and the change for November shows +1,500.00 (+15.0%)

#### Scenario: Previous month hint
- **WHEN** the check-in sheet is open for November 2026 and October's month-end balance is 10,000.00
- **THEN** the sheet shows "October: 10,000.00"

#### Scenario: Same as last month
- **WHEN** the user taps "Same as last month" on the sheet for November 2026 and October's month-end balance is 10,000.00
- **THEN** the amount is filled with 10,000.00 and can still be edited

#### Scenario: Start month has no previous month
- **WHEN** the sheet is open for the account's start month
- **THEN** no previous month hint and no "Same as last month" shortcut are shown

#### Scenario: Month that already has an entry
- **WHEN** the user picks a month that already has an entry
- **THEN** the amount is filled with that entry's balance, and saving replaces it

### Requirement: Months offered for a check-in
The check-in month choice SHALL offer only months from the account's start month through the current month. It SHALL NOT offer a future month or a month before the account was created.

#### Scenario: Months offered
- **WHEN** an account created in September 2026 opens the check-in sheet in November 2026
- **THEN** exactly November 2026, October 2026 and September 2026 are offered

#### Scenario: Created this month
- **WHEN** an account created in November 2026 opens the check-in sheet in November 2026
- **THEN** only November 2026 is offered

### Requirement: Check-in amount input
The check-in amount SHALL follow the same input and display rules as the Add Account amount. "Save" SHALL be enabled only while the amount holds a number.

#### Scenario: Empty amount
- **WHEN** the check-in sheet opens for a month with no entry and the amount is empty
- **THEN** "Save" is disabled

#### Scenario: Comma as separator
- **WHEN** the user types "11500,5" and saves
- **THEN** 11,500.50 is saved

### Requirement: Failed check-in keeps the sheet
If a check-in cannot be stored, the sheet SHALL stay open with the month and amount kept, SHALL show "Couldn't save the check-in. Try again.", and no entry SHALL change.

#### Scenario: Storage fails
- **WHEN** storing the check-in fails after the user taps "Save"
- **THEN** the sheet stays open with its entries kept and shows "Couldn't save the check-in. Try again."

### Requirement: No double check-in save
While a check-in is being saved, "Save" SHALL NOT start a second save.

#### Scenario: Double tap
- **WHEN** the user taps "Save" twice quickly on a valid check-in
- **THEN** the entry is stored once

### Requirement: Edit a monthly entry
Tapping a monthly entry in the timeline SHALL open the check-in sheet for that month with its amount filled in. Saving SHALL replace the entry and recalculate the later months' changes.

#### Scenario: Edit October
- **WHEN** the user taps the October 2026 row holding 10,000.00, changes it to 10,500.00 and saves
- **THEN** October 2026 holds 10,500.00 and November's change is recalculated against 10,500.00

### Requirement: Delete a monthly entry
The check-in sheet for a month that has an entry SHALL offer "Delete", except for the start month. Deleting SHALL ask for confirmation, then remove the entry; the month then carries forward the earlier balance and later changes are recalculated.

#### Scenario: Delete with confirmation
- **WHEN** the user opens October 2026 and taps "Delete"
- **THEN** a confirmation reads "Delete the check-in for October 2026?" with "Delete" and "Cancel"

#### Scenario: Confirm delete
- **WHEN** September is 9,000.00, October is 10,000.00, and the user confirms deleting October
- **THEN** October has no entry, its month-end balance is 9,000.00, and it is shown as "Not checked in"

#### Scenario: Cancel delete
- **WHEN** the user cancels the confirmation
- **THEN** the entry is unchanged and the sheet stays open

#### Scenario: Start month can't be deleted
- **WHEN** the user opens the start month's entry
- **THEN** its amount can be edited and no "Delete" action is offered

#### Scenario: Month without an entry
- **WHEN** the user opens a month that is "Not checked in"
- **THEN** no "Delete" action is offered

### Requirement: Failed delete keeps the entry
If a monthly entry cannot be deleted, the entry SHALL stay as it was and the sheet SHALL show "Couldn't delete the check-in. Try again."

#### Scenario: Storage fails on delete
- **WHEN** deleting October 2026 fails after the user confirms
- **THEN** October 2026 keeps its entry and the sheet shows "Couldn't delete the check-in. Try again."

### Requirement: Entries persist across restarts
Monthly entries SHALL be stored on the device and SHALL still be shown, with the same balances, after the app is closed and opened again.

#### Scenario: Check-in survives a restart
- **WHEN** the user checks in November 2026: 11,500.00, then closes and reopens the app
- **THEN** the account's balance is 11,500.00 and November 2026 shows 11,500.00

### Requirement: Fresh database
The first launch of a build with this storage SHALL start from an empty set of accounts. Accounts saved by earlier development builds SHALL NOT be carried over.

#### Scenario: Upgrade from a development build
- **WHEN** a device has accounts saved by an earlier development build and the user installs this build and opens the app
- **THEN** My Finances shows the "Add your first account" empty state
