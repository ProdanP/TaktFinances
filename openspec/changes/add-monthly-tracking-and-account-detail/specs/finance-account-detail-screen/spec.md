# Spec Delta

## Purpose

Defines the Account Detail screen, where the user reviews one account: its header with balance and monthly change, a chart of month-end balances, its timeline of entries, and the action that keeps the account up to date.

## ADDED Requirements

### Requirement: Open Account Detail
Account Detail SHALL show one account and SHALL offer a back action that returns to My Finances with that account's card still selected.

#### Scenario: Back to My Finances
- **WHEN** the user opens Account Detail for "Salary" from My Finances and goes back
- **THEN** My Finances is shown with the "Salary" card selected

### Requirement: Account Detail header
The header SHALL show the account's name, its bank badge or type icon, the same details summary as its card, the last 4 digits when recorded, and its current balance with the account's currency.

#### Scenario: Bank account header
- **WHEN** Account Detail opens for a Bank account "Salary", MAIB, Savings, last 4 digits "0421", balance 12,340.00 MDL
- **THEN** the header shows the MAIB badge, "Salary", "Savings · MAIB", "•••• 0421" and "12,340.00 MDL"

### Requirement: This month change in the header
The header SHALL show "This month" with the current month's change as an amount and percentage, signed and colored as success for an increase, error for a decrease and neutral for zero. When the account has no month-end balance for the previous month it SHALL show "—".

#### Scenario: Change this month
- **WHEN** October 2026 is 10,810.00 and the balance in November 2026 is 12,340.00
- **THEN** the header shows "This month +1,530.00 (+14.2%)" in the success color

#### Scenario: Account created this month
- **WHEN** the account was created in the current month
- **THEN** the header shows "This month —"

### Requirement: Balance chart
Account Detail SHALL show a line chart of the account's month-end balances with range options 6M, 1Y and All, where 6M is selected when the screen opens. Each range SHALL show the months of that range that the account has, ending at the current month.

#### Scenario: Default range
- **WHEN** Account Detail opens for an account with 14 months of history
- **THEN** 6M is selected and the chart shows the last 6 months

#### Scenario: All
- **WHEN** the user selects All for an account with 14 months of history
- **THEN** the chart shows all 14 months

#### Scenario: Shorter history than the range
- **WHEN** the account has 3 months of history and 1Y is selected
- **THEN** the chart shows those 3 months

### Requirement: Chart with one month
When the account has only one month of history the chart SHALL show a single point with the note "The chart fills in as months pass".

#### Scenario: New account
- **WHEN** Account Detail opens for an account created this month
- **THEN** the chart shows one point and "The chart fills in as months pass"

### Requirement: Chart point details
Tapping a chart point, or hovering it with a pointer, SHALL show that month and its month-end balance.

#### Scenario: Tap a point
- **WHEN** the user taps the October 2026 point holding 10,810.00
- **THEN** "October 2026" and "10,810.00" are shown for that point

### Requirement: Chart text summary
The chart SHALL have a text summary for screen readers naming the range, the first and last month shown and their balances.

#### Scenario: Screen reader
- **WHEN** a screen reader focuses the chart showing June to November 2026
- **THEN** it reads a summary that names June 2026 and November 2026 with their balances

### Requirement: Monthly timeline
Account Detail of a Monthly account SHALL list one row per month, newest first, from the current month back to the start month. Each row SHALL show the month, its month-end balance and its change against the previous month as an amount and percentage, with the percentage hidden when the previous balance is 0.00 and no change on the start month.

#### Scenario: Timeline rows
- **WHEN** the account started in September 2026 with 9,000.00, October is 10,000.00 and November is 11,500.00
- **THEN** the timeline shows November 2026 11,500.00 +1,500.00 (+15.0%), October 2026 10,000.00 +1,000.00 (+11.1%), and September 2026 9,000.00 with no change

### Requirement: Missing months in the timeline
A month with no entry SHALL show its carried-forward balance with a muted "Not checked in" label and a "Check in" shortcut that opens the check-in sheet for that month.

#### Scenario: Month not checked in
- **WHEN** October 2026 has no entry and September is 9,000.00
- **THEN** the October 2026 row shows 9,000.00, "Not checked in" and a "Check in" shortcut

#### Scenario: Check in from a missing month
- **WHEN** the user taps "Check in" on the October 2026 row
- **THEN** the check-in sheet opens with October 2026 selected

### Requirement: Check in action placement
Account Detail of a Monthly account SHALL show the "Check in" action above the timeline.

#### Scenario: Check in visible
- **WHEN** Account Detail opens for a Monthly account
- **THEN** a "Check in" action is shown above the timeline

### Requirement: Account tracked by trades
Account Detail of an Investment account SHALL show no "Check in" action and no monthly timeline. In place of the timeline it SHALL show "No trades yet" while the account has no trades.

#### Scenario: New Investment account
- **WHEN** Account Detail opens for a new Investment account "Brokerage" in EUR
- **THEN** the header shows "0.00 EUR", there is no "Check in" action, and the activity area shows "No trades yet"

### Requirement: Updates after a change
After a check-in is saved, edited or deleted, the header, chart and timeline SHALL show the new values without the user leaving the screen, and My Finances SHALL show the new balance when the user returns.

#### Scenario: Return after a check-in
- **WHEN** the user checks in 11,500.00 for the current month and goes back to My Finances
- **THEN** the account's card shows 11,500.00

### Requirement: Account cannot be loaded
If the account cannot be read, Account Detail SHALL show "Couldn't load this account" with a "Try again" action that reads it again.

#### Scenario: Read fails
- **WHEN** reading the account fails while Account Detail opens
- **THEN** "Couldn't load this account" and "Try again" are shown

#### Scenario: Retry succeeds
- **WHEN** the failure message is shown and the user taps "Try again" and reading succeeds
- **THEN** the header, chart and timeline are shown

### Requirement: Spoken amounts and changes
Amounts and changes on Account Detail SHALL have spoken labels that include the sign and the currency, and color SHALL NOT be the only signal of a change's direction.

#### Scenario: Screen reader reads a change
- **WHEN** a screen reader focuses "This month +1,530.00 (+14.2%)" on an MDL account
- **THEN** it reads the change as an increase of 1,530.00 MDL, 14.2 percent

### Requirement: Account Detail on phones and medium widths
When the available width does not fit two panes, Account Detail SHALL show the header, chart, action and timeline in one scrolling column, kept to a bounded width and centred when wider than a phone.

#### Scenario: Phone portrait
- **WHEN** Account Detail is open on a phone in portrait
- **THEN** the header, chart, "Check in" and timeline scroll together in one column

#### Scenario: Tablet portrait
- **WHEN** Account Detail is open on a tablet in portrait
- **THEN** the column is centred and does not span the full width

### Requirement: Account Detail two-pane layout on wide windows
When the available width fits two panes, Account Detail SHALL show the header and chart in one pane and the action and timeline in a second pane beside it, each scrolling on its own.

#### Scenario: Desktop window
- **WHEN** Account Detail is open in a wide desktop window
- **THEN** the header and chart are in one pane and "Check in" with the timeline is in a second pane beside it

#### Scenario: Narrowing the window
- **WHEN** the user narrows the window until two panes no longer fit
- **THEN** the screen shows one column and the selected chart range is kept
