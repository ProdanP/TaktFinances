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
Each card SHALL show the account's bank badge or type icon, its name, its amount with thousands separators, 2 decimal places and its currency, the last 4 digits when recorded, and the same one-line summary of details as the Add Account preview.

#### Scenario: Investment card
- **WHEN** an Investment account "Brokerage" with 1500.50 EUR, kind Stocks/ETF and platform "XTB" is shown
- **THEN** its card shows the Investment icon, "Brokerage", "1,500.50 EUR" and "Stocks/ETF · XTB"

#### Scenario: Loan card
- **WHEN** a Loan account "Car" with 500 MDL, direction Borrowed and counterparty "Ion" is shown
- **THEN** its card shows the Loan icon, "Car", "500.00 MDL" and "Borrowed · Ion", and no last 4 digits

### Requirement: Page indicator
When there is more than one account, My Finances SHALL show a page indicator below the cards with one mark per account and the selected account's mark highlighted. With exactly one account no indicator SHALL be shown.

#### Scenario: Indicator follows the swipe
- **WHEN** there are three accounts and the user swipes from the first to the second card
- **THEN** the indicator shows three marks with the second highlighted

#### Scenario: Single account
- **WHEN** there is exactly one account
- **THEN** no page indicator is shown

### Requirement: Selected account drives the area below
Below the cards My Finances SHALL show a "Recent activity" area that belongs to the selected account and updates when the selection changes. Until transactions exist it SHALL show a "No transactions yet" message.

#### Scenario: Activity for the selected account
- **WHEN** My Finances shows an account selected
- **THEN** a "Recent activity" heading is shown below the cards with the message "No transactions yet"

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
When the available width fits two panes, My Finances SHALL show the cards and page indicator in a pane of phone-like width beside a pane holding the "Recent activity" area of the selected account, which fills the remaining width. The width left after the navigation drawer or rail SHALL decide this, not the device type.

#### Scenario: Desktop window
- **WHEN** My Finances is shown in a wide desktop window with accounts
- **THEN** the cards are in one pane and "Recent activity" for the selected account fills a second pane beside it

#### Scenario: Selection updates the second pane
- **WHEN** the two panes are shown and the user moves to another card
- **THEN** the "Recent activity" pane belongs to the newly selected account

#### Scenario: Narrowing the window
- **WHEN** the user narrows the desktop window until two panes no longer fit
- **THEN** "Recent activity" moves below the cards and the selected card stays selected

#### Scenario: Empty and failure states stay single
- **WHEN** My Finances is shown in a wide window with no accounts, or after a failed read
- **THEN** the "Add your first account" card or the failure message is shown alone and centred, with no second pane

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
