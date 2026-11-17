# finance-accounts-overview Specification

## Purpose

Defines how My Finances presents the user's saved finance accounts: a swipeable row of account cards, the area driven by the selected card, the empty and failure states, and accounts persisting across app restarts.

## Requirements

### Requirement: Accounts persist across restarts
Accounts the user creates SHALL be stored on the device and SHALL still be shown on My Finances, with all their recorded fields, after the app is closed and opened again.

#### Scenario: Account survives a restart
- **WHEN** the user creates a Bank Account named "Salary" with 2,500.00 MDL, bank MAIB, kind Savings and last 4 digits "0421", then closes and reopens the app
- **THEN** My Finances shows a "Salary" card with the MAIB badge, "2,500.00 MDL", "•••• 0421" and "Savings · MAIB"

### Requirement: Account card swiper
My Finances SHALL show the saved accounts as a horizontal row of cards, one account per card, ordered from the oldest to the newest. The user SHALL move between cards by swiping, the selected card SHALL snap to the centre, and the neighbouring cards SHALL be partly visible at the edges.

#### Scenario: Swipe to the next account
- **WHEN** My Finances shows "Salary" selected and the user swipes left
- **THEN** the next account's card is centred and selected

#### Scenario: Order of cards
- **WHEN** the user created "Salary", then "Brokerage", then "Car loan"
- **THEN** the cards appear in the order Salary, Brokerage, Car loan

#### Scenario: Neighbours visible
- **WHEN** there are at least two accounts and the first is selected
- **THEN** part of the second card is visible at the trailing edge

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

### Requirement: Page indicator
When there is more than one account, My Finances SHALL show a page indicator below the cards with one mark per account and the selected account's mark highlighted. With exactly one account no indicator SHALL be shown.

#### Scenario: Indicator follows the swipe
- **WHEN** there are three accounts and the user swipes from the first to the second card
- **THEN** the indicator shows three marks with the second highlighted

#### Scenario: Single account
- **WHEN** there is exactly one account
- **THEN** no page indicator is shown

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

### Requirement: Empty state
When no account exists, My Finances SHALL show, in place of the cards, a single card inviting the user to add their first account. Tapping it SHALL open the Add Account screen. No page indicator and no "Recent activity" area SHALL be shown.

#### Scenario: First launch
- **WHEN** the user opens My Finances with no accounts
- **THEN** an "Add your first account" card is shown, with no page indicator and no "Recent activity" area

#### Scenario: Open Add Account from the empty state
- **WHEN** the user taps the "Add your first account" card
- **THEN** the Add Account screen is shown

### Requirement: Selection when returning to My Finances
After the user saves a new account, My Finances SHALL show that account's card selected. When the user returns without saving, the previously selected card SHALL stay selected.

#### Scenario: Land on the new account
- **WHEN** My Finances shows "Salary" selected among two accounts and the user adds an account "Car loan"
- **THEN** My Finances shows "Car loan" selected as the last card

#### Scenario: Cancel keeps the selection
- **WHEN** My Finances shows the second card selected and the user opens Add Account and cancels
- **THEN** the second card is still selected

### Requirement: Accounts cannot be loaded
If the saved accounts cannot be read, My Finances SHALL show a message that the accounts could not be loaded and a "Try again" action that reads them again. It SHALL NOT show the empty state in that case.

#### Scenario: Read fails
- **WHEN** reading the saved accounts fails
- **THEN** My Finances shows "Couldn't load your accounts" with a "Try again" action, and no "Add your first account" card

#### Scenario: Retry succeeds
- **WHEN** the failure message is shown and the user taps "Try again" and reading succeeds
- **THEN** the account cards are shown

### Requirement: My Finances on medium widths
When the space available to My Finances is wider than a phone but too narrow for two panes, the cards, page indicator and "Recent activity" area SHALL keep a phone-like width and SHALL be centred horizontally, stacked as on a phone.

#### Scenario: Tablet portrait
- **WHEN** My Finances is shown on a tablet in portrait next to the navigation rail
- **THEN** the selected card, the indicator and "Recent activity" share one centred column and are not stretched to the full width

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

### Requirement: Moving between cards without swiping
When the device has a mouse or trackpad, or the window fits two panes, My Finances SHALL show previous and next buttons beside the cards, and the left and right arrow keys SHALL select the previous and next card. The previous button SHALL be disabled on the first card and the next button on the last. On touch phones these buttons SHALL NOT be shown.

#### Scenario: Next button
- **WHEN** a desktop window shows the first of three cards and the user clicks the next button
- **THEN** the second card is selected and both buttons are enabled

#### Scenario: Arrow keys
- **WHEN** My Finances has focus on desktop showing the second card and the user presses the left arrow key
- **THEN** the first card is selected and the previous button is disabled

#### Scenario: Phone has no buttons
- **WHEN** My Finances is shown on a touch phone
- **THEN** no previous or next buttons are shown

### Requirement: Open Account Detail from a card
Tapping an account card SHALL open Account Detail for that account.

#### Scenario: Tap the selected card
- **WHEN** My Finances shows "Salary" selected and the user taps its card
- **THEN** Account Detail for "Salary" opens

#### Scenario: Keyboard
- **WHEN** the selected card has keyboard focus on desktop and the user presses Enter
- **THEN** Account Detail for that account opens
