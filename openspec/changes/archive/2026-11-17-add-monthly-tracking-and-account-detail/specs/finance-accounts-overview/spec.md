# Spec Delta

## MODIFIED Requirements

### Requirement: Account card content
Each card SHALL show the account's bank badge or type icon, its name, its current balance with thousands separators, 2 decimal places and its currency, the last 4 digits when recorded, and the same one-line summary of details as the Add Account preview.

#### Scenario: Investment card
- **WHEN** a new Investment account "Brokerage" in EUR, kind Stocks/ETF and platform "XTB" is shown
- **THEN** its card shows the Investment icon, "Brokerage", "0.00 EUR" and "Stocks/ETF · XTB"

#### Scenario: Loan card
- **WHEN** a Loan account "Car" with a balance of 500.00 MDL, direction Borrowed and counterparty "Ion" is shown
- **THEN** its card shows the Loan icon, "Car", "500.00 MDL" and "Borrowed · Ion", and no last 4 digits

#### Scenario: Card follows the latest check-in
- **WHEN** a Monthly account was created with 2,500.00 and the user later checks in 3,100.00 for the current month
- **THEN** its card shows "3,100.00"

### Requirement: Selected account drives the area below
Below the cards My Finances SHALL show a "Recent activity" area that belongs to the selected account and updates when the selection changes. For a Monthly account it SHALL list up to the 5 newest monthly entries, newest first, each with its month and balance, followed by a "See all" action that opens Account Detail. While the account has no entry besides its start month it SHALL show "No check-ins yet" with a "Check in" action that opens Account Detail with the check-in sheet open for the current month. For an Investment account with no trades it SHALL show "No trades yet".

#### Scenario: Activity for the selected account
- **WHEN** My Finances shows a Monthly account selected that has entries for September, October and November 2026
- **THEN** "Recent activity" lists November 2026, October 2026 and September 2026 with their balances, followed by "See all"

#### Scenario: At most five entries
- **WHEN** the selected Monthly account has entries for 8 months
- **THEN** "Recent activity" lists the 5 newest of them

#### Scenario: Only the start month
- **WHEN** the selected Monthly account has only its start month's entry
- **THEN** "Recent activity" shows "No check-ins yet" and a "Check in" action

#### Scenario: Check in from My Finances
- **WHEN** the user taps "Check in" in "Recent activity"
- **THEN** Account Detail for that account opens with the check-in sheet open for the current month

#### Scenario: Investment account
- **WHEN** the selected account is a new Investment account
- **THEN** "Recent activity" shows "No trades yet" with no action

#### Scenario: See all
- **WHEN** the user taps "See all"
- **THEN** Account Detail for the selected account opens

#### Scenario: Selection changes
- **WHEN** the user swipes to another account
- **THEN** the "Recent activity" area belongs to the newly selected account

### Requirement: My Finances two-pane layout on wide windows
When the available width fits two panes, My Finances SHALL be shown in a list pane beside a detail pane. The list pane SHALL hold the cards, page indicator and "Recent activity" stacked as on a phone. The detail pane SHALL hold Account Detail of the opened account, or "Open an account to see its history" before an account is opened. The width left after the navigation drawer or rail SHALL decide this, not the device type.

#### Scenario: Desktop window
- **WHEN** My Finances is shown in a wide desktop window with accounts and no account opened
- **THEN** the cards and "Recent activity" are in one pane and "Open an account to see its history" is in a second pane beside it

#### Scenario: Selection updates the second pane
- **WHEN** the two panes are shown and the user moves to another card
- **THEN** "Recent activity" belongs to the newly selected account

#### Scenario: Narrowing the window
- **WHEN** the user narrows the desktop window until two panes no longer fit and no account is opened
- **THEN** My Finances is shown alone with "Recent activity" below the cards, and the selected card stays selected

#### Scenario: Empty and failure states stay single
- **WHEN** My Finances is shown in a wide window with no accounts, or after a failed read
- **THEN** the list pane shows the "Add your first account" card or the failure message, and the detail pane shows "Open an account to see its history"

## ADDED Requirements

### Requirement: Open Account Detail from a card
Tapping an account card SHALL open Account Detail for that account.

#### Scenario: Tap the selected card
- **WHEN** My Finances shows "Salary" selected and the user taps its card
- **THEN** Account Detail for "Salary" opens

#### Scenario: Keyboard
- **WHEN** the selected card has keyboard focus on desktop and the user presses Enter
- **THEN** Account Detail for that account opens
