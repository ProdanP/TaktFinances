# Spec Delta

## MODIFIED Requirements

### Requirement: Common form fields
The Add Account screen SHALL offer an account type choice of Bank, Investment and Loan, each shown with an icon, a required name, a currency choice and, for Bank and Loan, a required amount. Investment SHALL NOT show an amount. Bank SHALL be selected by default and the name and amount SHALL start empty.

#### Scenario: Initial form
- **WHEN** the Add Account screen opens
- **THEN** Bank and MDL are selected, the name and amount are empty, and the type choice shows Bank, Investment and Loan with an icon each

#### Scenario: Choose a type
- **WHEN** the user selects Loan
- **THEN** Loan is the selected type and the name, currency and amount entries are kept

#### Scenario: Investment hides the amount
- **WHEN** the user enters amount "500" and selects Investment
- **THEN** no amount is shown, and the currency choice is still offered

#### Scenario: Amount comes back
- **WHEN** the user then selects Bank again
- **THEN** the amount is shown and still holds "500"

### Requirement: Save action availability
The screen SHALL show an "Add account" action fixed at the bottom of the screen. It SHALL be enabled only when the name is not blank, the amount holds a number when the selected type shows an amount, and every required details field of the selected type is valid; otherwise it SHALL be disabled.

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

#### Scenario: Investment needs no amount
- **WHEN** Investment is selected with a name, a kind and a platform, and the amount is empty
- **THEN** "Add account" is enabled and the hint does not name the amount

### Requirement: Create an account
Tapping an enabled "Add account" SHALL create an account with the chosen type, the trimmed name, the chosen currency, the details of its type and today as its creation date, store it on the device, then return the user to My Finances with the new account's card selected. A Bank or Loan account SHALL be tracked in Monthly mode with the entered amount as the current month's entry. An Investment account SHALL start at 0.00 and be tracked by trades.

#### Scenario: Successful save
- **WHEN** the user selects Investment, selects EUR, enters name "Brokerage", kind Stocks/ETF and platform "XTB", and taps "Add account"
- **THEN** an Investment account named "Brokerage" with a balance of 0.00 EUR is created and My Finances is shown with the "Brokerage" card selected

#### Scenario: Monthly account saved
- **WHEN** in November 2026 the user creates a Bank account "Salary" with amount "2500" in MDL
- **THEN** the account is tracked in Monthly mode with one entry, November 2026: 2,500.00, and My Finances shows "Salary" with 2,500.00 MDL

#### Scenario: Cancel
- **WHEN** the user navigates back or cancels without saving
- **THEN** no account is created and My Finances is shown

## ADDED Requirements

### Requirement: Amount label
For Bank and Loan the amount SHALL be labelled "Balance this month", making clear that it is the account's balance for the current month.

#### Scenario: Bank amount label
- **WHEN** the Add Account screen opens with Bank selected
- **THEN** the amount is labelled "Balance this month"

### Requirement: Investment tracking notice
When Investment is selected the form SHALL show, in place of the amount, the note "Investments are tracked by trades. You'll add your holdings as trades after saving." The live preview SHALL show 0.00 with the selected currency.

#### Scenario: Investment selected
- **WHEN** the user selects Investment and currency USD
- **THEN** the form shows "Investments are tracked by trades. You'll add your holdings as trades after saving." and the preview shows "0.00 USD"

### Requirement: No tracking choice yet
The Add Account form SHALL NOT offer a tracking mode choice. Bank and Loan accounts SHALL be created in Monthly mode.

#### Scenario: No tracking choice
- **WHEN** the Add Account screen is shown for any type
- **THEN** no tracking mode choice is offered
