# Spec Delta

## Purpose

Lets a user start creating a finance account from the My Finances tab and record it through a minimal form with an account type, name, currency, and amount.

## ADDED Requirements

### Requirement: My Finances offers an add action
The My Finances tab SHALL show a top bar with an add ("+") action that opens the Add Account screen. Other Home tabs SHALL NOT show it.

#### Scenario: Open Add Account
- **WHEN** the user is on My Finances and taps "+"
- **THEN** the Add Account screen is shown

#### Scenario: Other tabs have no add action
- **WHEN** the user is on Dashboard or Profile
- **THEN** no add action is shown

### Requirement: Add Account form fields
The Add Account screen SHALL present an account type choice of Bank Account, Investment, or Loan, a required name field, a currency choice, and a required amount field. Bank Account SHALL be selected by default. The currency choice SHALL offer MDL, USD, EUR, GBP, RON, CHF, and JPY, with MDL selected by default.

#### Scenario: Initial form
- **WHEN** the Add Account screen opens
- **THEN** Bank Account and MDL are selected, and name and amount are empty

#### Scenario: Choose a currency
- **WHEN** the user selects EUR
- **THEN** EUR is the selected currency and the other entries are kept

#### Scenario: Choose a type
- **WHEN** the user selects Loan
- **THEN** Loan is the selected type and the name and amount entries are kept

### Requirement: Validation before saving
The system MUST NOT create an account when the name is blank or the amount is not a non-negative decimal number. The screen SHALL show which field is invalid and stay open.

#### Scenario: Blank name
- **WHEN** the user saves with a blank name
- **THEN** no account is created and a name error is shown

#### Scenario: Invalid amount
- **WHEN** the user saves with an amount of "abc" or a negative number
- **THEN** no account is created and an amount error is shown

#### Scenario: Error clears on edit
- **WHEN** a field shows an error and the user edits that field
- **THEN** that field's error is cleared

### Requirement: Create an account
When the input is valid, saving SHALL create an account with the chosen type, the trimmed name, the chosen currency, and the entered amount, then return the user to My Finances.

#### Scenario: Successful save
- **WHEN** the user selects Investment, selects EUR, enters name "Brokerage" and amount "1500.50", and saves
- **THEN** an Investment account named "Brokerage" with amount 1500.50 EUR is created and My Finances is shown

#### Scenario: Cancel
- **WHEN** the user navigates back or cancels without saving
- **THEN** no account is created and My Finances is shown
