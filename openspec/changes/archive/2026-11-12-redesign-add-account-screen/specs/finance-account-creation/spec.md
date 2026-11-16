# Spec Delta

## Purpose

Lets a user create a finance account from My Finances through the Add Account screen: choose a type, enter a name, currency and amount, see a live preview of the account, and save it once the form is complete.

## ADDED Requirements

### Requirement: My Finances offers an add action
The My Finances tab SHALL show a top bar with an add ("+") action that opens the Add Account screen. Other Home tabs SHALL NOT show it.

#### Scenario: Open Add Account
- **WHEN** the user is on My Finances and taps "+"
- **THEN** the Add Account screen is shown

#### Scenario: Other tabs have no add action
- **WHEN** the user is on Dashboard or Profile
- **THEN** no add action is shown

### Requirement: Common form fields
The Add Account screen SHALL offer an account type choice of Bank, Investment and Loan, each shown with an icon, a required name, a currency choice and a required amount. Bank SHALL be selected by default and the name and amount SHALL start empty.

#### Scenario: Initial form
- **WHEN** the Add Account screen opens
- **THEN** Bank and MDL are selected, the name and amount are empty, and the type choice shows Bank, Investment and Loan with an icon each

#### Scenario: Choose a type
- **WHEN** the user selects Loan
- **THEN** Loan is the selected type and the name, currency and amount entries are kept

### Requirement: Supported currencies
The currency choice SHALL offer exactly MDL, USD, EUR, GBP and RON, with MDL selected by default. Every supported currency SHALL use exactly 2 decimal places.

#### Scenario: Currency list
- **WHEN** the user opens the currency choice
- **THEN** MDL, USD, EUR, GBP and RON are offered and no other currency

#### Scenario: Choose a currency
- **WHEN** the user selects EUR
- **THEN** EUR is the selected currency and the other entries are kept

### Requirement: Currency shown next to the amount
The selected currency SHALL be shown directly next to the amount, and choosing a currency SHALL be possible from that position.

#### Scenario: Change currency from the amount
- **WHEN** the user taps the currency shown next to the amount and selects USD
- **THEN** USD is shown next to the amount

### Requirement: Amount entry
The amount input SHALL accept only digits and a single decimal separator, where both "." and "," count as the separator. It SHALL NOT hold more than 2 digits after the separator, and pasted text SHALL be reduced to what the input accepts.

#### Scenario: Comma as separator
- **WHEN** the user types "2500,5"
- **THEN** the amount is taken as 2500.50

#### Scenario: Third decimal is not accepted
- **WHEN** the user types "3.999"
- **THEN** the input holds "3.99"

#### Scenario: Letters and signs are not accepted
- **WHEN** the user types or pastes "-12a.5"
- **THEN** the input holds "12.5"

#### Scenario: Second separator is not accepted
- **WHEN** the user types "1.2.3"
- **THEN** the input holds "1.23"

### Requirement: Amount display
While the amount is not being edited it SHALL be shown with thousands separators and exactly 2 decimal places. While it is being edited it SHALL show the raw entered text. An empty amount SHALL show a muted "0.00" placeholder.

#### Scenario: Formatted after editing
- **WHEN** the user enters "2500.5" and moves to another field
- **THEN** the amount reads "2,500.50"

#### Scenario: Whole number padded
- **WHEN** the user enters "2500" and moves to another field
- **THEN** the amount reads "2,500.00"

#### Scenario: Raw while editing
- **WHEN** the user returns to an amount that reads "2,500.50"
- **THEN** the input shows "2500.50" for editing

#### Scenario: Empty amount
- **WHEN** the amount is empty
- **THEN** a muted "0.00" placeholder is shown

### Requirement: Live account preview
The screen SHALL show, above the form fields, a preview of the account being created that updates as the user edits. It SHALL show the selected bank's badge or the type's icon, the name or an "Account name" placeholder, the amount with its currency, the last 4 digits when entered, and a summary of the chosen details.

#### Scenario: Empty preview
- **WHEN** the Add Account screen opens
- **THEN** the preview shows the Bank type icon, "Account name" as a placeholder, a muted "0.00" and MDL

#### Scenario: Preview follows the bank form
- **WHEN** the user enters name "Salary", amount "2500", last 4 digits "0421", and selects Victoriabank and Savings
- **THEN** the preview shows the Victoriabank badge, "Salary", "2,500.00 MDL", "•••• 0421" and "Savings · Victoriabank"

#### Scenario: Preview follows the investment form
- **WHEN** the user selects Investment, kind Crypto, platform "Binance" and currency USD
- **THEN** the preview summary reads "Crypto · Binance" and the currency shows USD

### Requirement: Save action availability
The screen SHALL show an "Add account" action fixed at the bottom of the screen. It SHALL be enabled only when the name is not blank, the amount holds a number, and every required details field of the selected type is valid; otherwise it SHALL be disabled.

#### Scenario: Disabled on open
- **WHEN** the Add Account screen opens
- **THEN** "Add account" is disabled

#### Scenario: Enabled when complete
- **WHEN** the user fills every required field of the selected type with valid values
- **THEN** "Add account" is enabled

#### Scenario: Disabled again when a field is cleared
- **WHEN** "Add account" is enabled and the user clears the name
- **THEN** "Add account" is disabled

#### Scenario: Trailing separator counts as a whole number
- **WHEN** every other field is valid and the amount is "12."
- **THEN** "Add account" is enabled and saving records 12.00

### Requirement: Missing fields hint
While "Add account" is disabled, the screen SHALL show next to it a short hint that names every required field still missing or invalid, in form order. The hint SHALL update as the user edits and SHALL disappear when the action becomes enabled.

#### Scenario: Hint on open
- **WHEN** the Add Account screen opens with Bank selected
- **THEN** the hint names the name, the amount, the bank and the account kind

#### Scenario: Hint shrinks while filling
- **WHEN** the user then enters a name and an amount
- **THEN** the hint names only the bank and the account kind

#### Scenario: Hint disappears
- **WHEN** the last missing field is filled
- **THEN** no hint is shown and "Add account" is enabled

### Requirement: Errors on edited fields
A field SHALL show its error only after the user has edited it and moved away from it while its content is invalid. Fields the user has not edited SHALL NOT show errors. Editing a field that shows an error SHALL clear that error.

#### Scenario: Untouched form is calm
- **WHEN** the Add Account screen opens
- **THEN** no field shows an error

#### Scenario: Name cleared and left
- **WHEN** the user types a name, deletes it, and moves to another field
- **THEN** the name shows "Enter a name"

#### Scenario: Error clears on edit
- **WHEN** the name shows an error and the user types a character into it
- **THEN** the name error is cleared

### Requirement: Create an account
Tapping an enabled "Add account" SHALL create an account with the chosen type, the trimmed name, the chosen currency, the entered amount and the details of its type, then return the user to My Finances.

#### Scenario: Successful save
- **WHEN** the user selects Investment, selects EUR, enters name "Brokerage", amount "1500.50", kind Stocks/ETF and platform "XTB", and taps "Add account"
- **THEN** an Investment account named "Brokerage" with amount 1500.50 EUR is created and My Finances is shown

#### Scenario: Cancel
- **WHEN** the user navigates back or cancels without saving
- **THEN** no account is created and My Finances is shown
