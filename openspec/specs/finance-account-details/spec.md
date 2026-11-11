# finance-account-details Specification

## Purpose

Defines the type-specific details a finance account records (bank, kind, last 4 digits, platform, loan direction, counterparty) and how the Add Account form collects and validates them.

## Requirements

### Requirement: Details block follows the account type
The Add Account form SHALL show, below the name, currency and amount fields, a details block that matches the selected account type. Bank Account, Investment and Loan SHALL each show only their own fields.

#### Scenario: Bank Account selected
- **WHEN** the Add Account screen opens with Bank Account selected
- **THEN** the bank, account kind and last 4 digits fields are shown, and no investment or loan fields are shown

#### Scenario: Investment selected
- **WHEN** the user selects Investment
- **THEN** the investment kind and platform fields are shown, and no bank or loan fields are shown

#### Scenario: Loan selected
- **WHEN** the user selects Loan
- **THEN** the direction and counterparty fields are shown, and no bank or investment fields are shown

### Requirement: Switching type resets details
Selecting a different account type SHALL keep the name, currency and amount entries and SHALL clear all details entries and their errors.

#### Scenario: Switch after filling details
- **WHEN** the user picks a bank, enters last 4 digits, then selects Loan
- **THEN** name, currency and amount are unchanged and no bank or last 4 digits value is carried into the Loan details

### Requirement: Bank choice with icons
For a Bank Account the form SHALL offer a bank choice of Moldova Agroindbank (MAIB), Victoriabank, Moldindconbank (MICB), ProCredit Bank, Mobiasbancă, and Other. Each entry SHALL show an icon next to its name, and the selected bank's icon SHALL be visible on the field. No bank SHALL be preselected.

#### Scenario: Open bank choice
- **WHEN** the user opens the bank choice
- **THEN** the six entries are listed, each with an icon

#### Scenario: Select a bank
- **WHEN** the user selects Victoriabank
- **THEN** the bank field shows Victoriabank with its icon

#### Scenario: Other bank
- **WHEN** the user selects Other
- **THEN** the bank field shows Other and no further text input appears

### Requirement: Bank account kind and last 4 digits
For a Bank Account the form SHALL require an account kind of Current, Savings, Card or Deposit, and SHALL accept an optional last 4 digits value. The last 4 digits value MUST be exactly 4 decimal digits when present, and the field SHALL NOT accept more than 4 characters.

#### Scenario: Valid last 4 digits
- **WHEN** the user enters "0421" and saves with all other bank fields valid
- **THEN** the account is created with last 4 digits "0421"

#### Scenario: Last 4 digits omitted
- **WHEN** the user leaves last 4 digits empty and saves with all other bank fields valid
- **THEN** the account is created with no last 4 digits

#### Scenario: Wrong length or characters
- **WHEN** the user saves with last 4 digits "12", "12a4" or "123"
- **THEN** no account is created and a last 4 digits error is shown

#### Scenario: Full number cannot be entered
- **WHEN** the user types or pastes "1234567890123456" into the last 4 digits field
- **THEN** the field holds at most 4 characters

### Requirement: Investment details
For an Investment the form SHALL require an investment kind of Stocks/ETF, Crypto, Bonds, Deposit, Real estate or Other, and SHALL require a non-blank platform entered as free text. The platform SHALL be trimmed before saving.

#### Scenario: Valid investment
- **WHEN** the user selects kind Crypto, enters platform "  Binance " and saves with valid name and amount
- **THEN** an Investment account is created with kind Crypto and platform "Binance"

#### Scenario: Blank platform
- **WHEN** the user saves an Investment with a blank platform
- **THEN** no account is created and a platform error is shown

### Requirement: Loan direction and counterparty
For a Loan the form SHALL require a direction of Borrowed or Lent and a non-blank counterparty entered as free text. The counterparty SHALL be trimmed before saving. The amount SHALL remain a non-negative number for both directions, and the form SHALL NOT apply a sign to it.

#### Scenario: Borrowed loan
- **WHEN** the user selects Borrowed, enters counterparty "Ion" and amount "500", and saves with a valid name
- **THEN** a Loan account is created with direction Borrowed, counterparty "Ion" and amount 500

#### Scenario: Lent loan
- **WHEN** the user selects Lent, enters counterparty "Ana" and amount "200", and saves with a valid name
- **THEN** a Loan account is created with direction Lent, counterparty "Ana" and amount 200

#### Scenario: Blank counterparty
- **WHEN** the user saves a Loan with a blank counterparty
- **THEN** no account is created and a counterparty error is shown

#### Scenario: Negative amount stays invalid
- **WHEN** the user saves a Loan with amount "-100"
- **THEN** no account is created and an amount error is shown

### Requirement: Required details block saving
The system MUST NOT create an account when a required details field for the selected type is missing or invalid. Every invalid field, including name and amount, SHALL be reported in the same save attempt, and the screen SHALL stay open. Editing a field SHALL clear that field's error.

#### Scenario: Bank not chosen
- **WHEN** the user saves a Bank Account without choosing a bank
- **THEN** no account is created and a bank error is shown

#### Scenario: Several problems at once
- **WHEN** the user saves a Loan with a blank name, no direction chosen and a blank counterparty
- **THEN** the name, direction and counterparty errors are all shown

#### Scenario: Error clears on edit
- **WHEN** a details field shows an error and the user edits that field
- **THEN** that field's error is cleared

### Requirement: Saved account keeps its details
A successful save SHALL create an account that records the common fields together with the details of its type only, then return the user to My Finances.

#### Scenario: Bank account saved
- **WHEN** the user selects MAIB and kind Savings, enters last 4 digits "0421", name "Salary", amount "2500" in MDL, and saves
- **THEN** a Bank Account named "Salary" is created with 2500 MDL, bank MAIB, kind Savings and last 4 digits "0421", and My Finances is shown
