## ADDED Requirements

### Requirement: Account actions in the top bar
Once the account is loaded, the Account Detail top bar SHALL show an "Edit" action and an overflow menu that holds "Delete account". While the account is loading, or after it failed to load, neither SHALL be shown.

#### Scenario: Loaded account
- **WHEN** Account Detail of "Salary" has loaded
- **THEN** the top bar shows "Edit" and an overflow menu containing "Delete account"

#### Scenario: Load failed
- **WHEN** Account Detail shows "Couldn't load this account"
- **THEN** the top bar shows neither "Edit" nor the overflow menu

#### Scenario: Screen reader
- **WHEN** a screen reader focuses the overflow menu button
- **THEN** it reads "More options"

## MODIFIED Requirements

### Requirement: Updates after a change
After a check-in is saved, edited or deleted, or the account is edited, the header, chart and timeline SHALL show the new values without the user leaving the screen. My Finances SHALL show the new name and balance when the user returns to it, and right away when it is shown beside Account Detail.

#### Scenario: Return after a check-in
- **WHEN** the user checks in 11,500.00 for the current month and goes back to My Finances
- **THEN** the account's card shows 11,500.00

#### Scenario: Edit beside My Finances
- **WHEN** Account Detail of "Salary" is beside My Finances and the user renames it to "Daily card" and sets its balance this month to 3,000.00
- **THEN** the header and the "Salary" card both show "Daily card" and 3,000.00 without leaving the screen

#### Scenario: Check-in beside My Finances
- **WHEN** Account Detail is beside My Finances and the user checks in 11,500.00 for the current month
- **THEN** the account's card on My Finances shows 11,500.00
