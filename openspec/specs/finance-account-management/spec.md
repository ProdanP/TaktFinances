# finance-account-management Specification

## Purpose

Defines how the user changes or removes an account after creating it: editing its name and current balance, and deleting it with all its history.

## Requirements

### Requirement: Open Edit account
Account Detail SHALL offer an "Edit" action that opens a sheet titled "Edit account" with a "Save" action. The sheet SHALL be filled with the account's name and, for a Monthly account, its current balance in a field labelled "Balance this month".

#### Scenario: Edit a Monthly account
- **WHEN** the user taps "Edit" on Account Detail of a Bank account "Salary" whose current balance is 2,500.00 MDL
- **THEN** a sheet titled "Edit account" opens with the name "Salary" and "Balance this month" showing 2,500.00 with "MDL"

#### Scenario: Edit an Investment account
- **WHEN** the user taps "Edit" on Account Detail of an Investment account "Brokerage"
- **THEN** the sheet shows the name "Brokerage" and no amount field

#### Scenario: Close without saving
- **WHEN** the user changes the name in the sheet and dismisses it without tapping "Save"
- **THEN** the sheet closes and the account is unchanged

### Requirement: Only name and current balance are editable
The edit sheet SHALL NOT offer the account's type, currency, details or tracking mode. They stay as they were when the account was created.

#### Scenario: Fixed fields
- **WHEN** the edit sheet is open for a Bank account with bank MAIB, kind Savings and last 4 digits "0421"
- **THEN** none of type, currency, bank, kind, last 4 digits or tracking mode can be changed in it

### Requirement: Edited name
The name SHALL follow the Add Account rule: "Save" SHALL be disabled while the name is blank, and the saved name SHALL be trimmed.

#### Scenario: Blank name
- **WHEN** the user clears the name in the edit sheet
- **THEN** "Save" is disabled

#### Scenario: Rename
- **WHEN** the user changes the name to "  Daily card " and taps "Save"
- **THEN** the sheet closes and Account Detail and its card on My Finances show "Daily card"

### Requirement: Edited balance sets this month
For a Monthly account, saving a "Balance this month" that differs from the current balance SHALL store it as the current month's entry, replacing an entry the month already has. Entries of earlier months SHALL NOT change.

#### Scenario: Month already checked in
- **WHEN** November 2026 is the current month with an entry of 3,000.00 and the user saves 3,250.00 in the edit sheet
- **THEN** November 2026 is 3,250.00, the header shows 3,250.00 and earlier months are unchanged

#### Scenario: Month carried forward
- **WHEN** November 2026 is the current month with no entry, October is 2,500.00, and the user saves 2,900.00 in the edit sheet
- **THEN** November 2026 has an entry of 2,900.00 and is no longer "Not checked in", and October stays 2,500.00

#### Scenario: Balance entry rules
- **WHEN** the user clears "Balance this month" in the edit sheet
- **THEN** "Save" is disabled, and the field accepts input with the same rules as the check-in amount

### Requirement: Unchanged values are not written
An unchanged "Balance this month" SHALL NOT store an entry. When neither the name nor the balance changed, "Save" SHALL close the sheet without storing anything.

#### Scenario: Rename only on a carried-forward month
- **WHEN** November 2026 has no entry and carries forward 2,500.00, and the user changes only the name and taps "Save"
- **THEN** the name is saved and November 2026 still shows "Not checked in"

#### Scenario: Nothing changed
- **WHEN** the user opens the edit sheet and taps "Save" without changing anything
- **THEN** the sheet closes and nothing is stored

### Requirement: Edit saves all or nothing
A name and balance saved together SHALL be stored together or not at all. If storing fails, the sheet SHALL stay open with what the user entered and SHALL show "Couldn't save the changes. Try again.", and the account SHALL be unchanged.

#### Scenario: Storage fails
- **WHEN** the user changes the name and the balance and storing them fails after "Save"
- **THEN** the sheet stays open with both values kept, shows "Couldn't save the changes. Try again.", and Account Detail still shows the old name and balance

### Requirement: No double edit save
While an edit is being saved, "Save" SHALL NOT start a second save.

#### Scenario: Double tap
- **WHEN** the user taps "Save" twice quickly in the edit sheet
- **THEN** the changes are stored once

### Requirement: Delete account confirmation
Account Detail SHALL offer "Delete account" in its overflow menu. Choosing it SHALL show a dialog saying "Delete "<name>"? This removes the account and all its history, including past months on the Dashboard. This can't be undone." with a destructive "Delete" action and a "Cancel" action.

#### Scenario: Ask before deleting
- **WHEN** the user chooses "Delete account" on Account Detail of "Salary"
- **THEN** a dialog asks "Delete "Salary"? This removes the account and all its history, including past months on the Dashboard. This can't be undone." with "Delete" and "Cancel"

#### Scenario: Cancel
- **WHEN** the delete dialog is shown and the user taps "Cancel"
- **THEN** the dialog closes and the account is unchanged

### Requirement: Delete removes the account and its history
Confirming the delete SHALL remove the account and every entry it has, and SHALL keep them removed after the app restarts.

#### Scenario: Confirm delete
- **WHEN** the user confirms deleting "Salary", which has entries for three months
- **THEN** "Salary" and its entries are gone, and they are still gone after the app is closed and opened again

### Requirement: Neighbour selected after a delete
After a delete, My Finances SHALL select the card that followed the deleted one, or the card before it when the deleted card was last. With no accounts left, My Finances SHALL show its empty state.

#### Scenario: Middle card
- **WHEN** the cards are Salary, Brokerage, Car loan and the user deletes "Brokerage"
- **THEN** My Finances shows "Car loan" selected

#### Scenario: Last card
- **WHEN** the cards are Salary, Brokerage, Car loan and the user deletes "Car loan"
- **THEN** My Finances shows "Brokerage" selected

#### Scenario: Only account
- **WHEN** the user deletes the only account
- **THEN** My Finances shows the "Add your first account" empty state

### Requirement: Where the user lands after a delete
When Account Detail is the only pane, a confirmed delete SHALL close it and show My Finances. When it is beside My Finances, the detail pane SHALL show Account Detail of the neighbour selected on My Finances, or "Open an account to see its history" when no account is left.

#### Scenario: Phone
- **WHEN** the user deletes "Brokerage" from Account Detail on a phone, with cards Salary, Brokerage, Car loan
- **THEN** Account Detail closes and My Finances shows "Car loan" selected

#### Scenario: Two panes
- **WHEN** Account Detail of "Brokerage" is beside My Finances and the user deletes it, with cards Salary, Brokerage, Car loan
- **THEN** the cards show Salary and Car loan with "Car loan" selected, and the detail pane shows Account Detail of "Car loan"

#### Scenario: Two panes, last account
- **WHEN** Account Detail of the only account is beside My Finances and the user deletes it
- **THEN** My Finances shows its empty state and the detail pane shows "Open an account to see its history"

### Requirement: Failed delete keeps the account
If the delete cannot be stored, the account and its entries SHALL be kept, Account Detail SHALL stay open, and it SHALL show "Couldn't delete the account. Try again."

#### Scenario: Storage fails on delete
- **WHEN** the user confirms deleting "Salary" and removing it fails
- **THEN** Account Detail of "Salary" stays open with its values and shows "Couldn't delete the account. Try again."

### Requirement: No double delete
While a delete is in progress, Account Detail SHALL NOT start another delete or an edit save.

#### Scenario: Delete in progress
- **WHEN** a confirmed delete has not finished yet
- **THEN** "Edit" and "Delete account" do nothing
