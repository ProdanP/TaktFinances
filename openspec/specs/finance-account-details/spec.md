# finance-account-details Specification

## Purpose

Defines the type-specific details a finance account records (bank, kind, last 4 digits, platform, loan direction, counterparty) and how the Add Account form collects and validates them.

## Requirements

### Requirement: Details block follows the account type
The Add Account form SHALL show, below the name field, a details block that matches the selected account type. Bank, Investment and Loan SHALL each show only their own fields. Choices with a fixed list of options SHALL be shown directly on the form as selectable options, not inside a menu.

#### Scenario: Bank Account selected
- **WHEN** the Add Account screen opens with Bank selected
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

#### Scenario: Save becomes unavailable after switching
- **WHEN** a complete Bank form has "Add account" enabled and the user selects Loan
- **THEN** "Add account" is disabled until the Loan details are filled

### Requirement: Bank choice with icons
For a Bank Account the form SHALL offer a bank choice of Moldova Agroindbank (MAIB), Victoriabank, Moldindconbank (MICB), ProCredit Bank, Mobiasbancă, and Other, shown as a horizontally scrollable row of bank badges with a short name under each, in that order with Other last. The selected bank SHALL be highlighted. No bank SHALL be preselected.

#### Scenario: Open bank choice
- **WHEN** the Bank form is shown
- **THEN** the six banks are shown as badges in one scrollable row, Other last, and none is highlighted

#### Scenario: Select a bank
- **WHEN** the user taps Victoriabank
- **THEN** the Victoriabank badge is highlighted and the preview shows the Victoriabank badge

#### Scenario: Other bank
- **WHEN** the user taps Other
- **THEN** the Other badge is highlighted and no further text input appears

### Requirement: Bank account kind and last 4 digits
For a Bank Account the form SHALL require an account kind of Current, Savings, Card or Deposit, offered as single-select chips with none preselected, and SHALL accept an optional last 4 digits value. The last 4 digits value MUST be exactly 4 decimal digits when present, and the field SHALL NOT accept more than 4 characters.

#### Scenario: Choose an account kind
- **WHEN** the user taps the Savings chip and then the Card chip
- **THEN** only Card is selected

#### Scenario: Valid last 4 digits
- **WHEN** the user enters "0421" and saves with all other bank fields valid
- **THEN** the account is created with last 4 digits "0421"

#### Scenario: Last 4 digits omitted
- **WHEN** the user leaves last 4 digits empty and saves with all other bank fields valid
- **THEN** the account is created with no last 4 digits

#### Scenario: Wrong length or characters
- **WHEN** the last 4 digits field holds "12" or "123"
- **THEN** "Add account" is disabled, and once the user leaves the field it shows "Enter exactly 4 digits"

#### Scenario: Full number cannot be entered
- **WHEN** the user types or pastes "1234567890123456" into the last 4 digits field
- **THEN** the field holds at most 4 characters

### Requirement: Investment details
For an Investment the form SHALL require an investment kind of Stocks/ETF, Crypto, Bonds, Deposit, Real estate or Other, offered as single-select chips that wrap onto more than one line when needed, with none preselected, and SHALL require a non-blank platform entered as free text. The platform SHALL be trimmed before saving.

#### Scenario: All kinds visible
- **WHEN** the Investment form is shown on a phone
- **THEN** all six investment kind chips are visible without scrolling sideways

#### Scenario: Valid investment
- **WHEN** the user selects kind Crypto, enters platform "  Binance " and saves with valid name and amount
- **THEN** an Investment account is created with kind Crypto and platform "Binance"

#### Scenario: Blank platform
- **WHEN** the platform is blank
- **THEN** "Add account" is disabled and the hint names the platform

### Requirement: Loan direction and counterparty
For a Loan the form SHALL require a direction of Borrowed or Lent, offered as a two-option toggle with none preselected, and a non-blank counterparty entered as free text. The counterparty SHALL be trimmed before saving. The amount SHALL remain a non-negative number for both directions, and the form SHALL NOT apply a sign to it.

#### Scenario: Borrowed loan
- **WHEN** the user selects Borrowed, enters counterparty "Ion" and amount "500", and saves with a valid name
- **THEN** a Loan account is created with direction Borrowed, counterparty "Ion" and amount 500

#### Scenario: Lent loan
- **WHEN** the user selects Lent, enters counterparty "Ana" and amount "200", and saves with a valid name
- **THEN** a Loan account is created with direction Lent, counterparty "Ana" and amount 200

#### Scenario: Blank counterparty
- **WHEN** the counterparty is blank
- **THEN** "Add account" is disabled and the hint names the counterparty

#### Scenario: Negative amount stays invalid
- **WHEN** the user types "-100" into the amount of a Loan
- **THEN** the amount holds "100"

### Requirement: Required details block saving
The system MUST NOT create an account while a required details field for the selected type is missing or invalid: "Add account" SHALL stay disabled and the missing fields hint SHALL name each such field. An invalid details field SHALL show its error once the user has edited and left it, and editing a field SHALL clear that field's error.

#### Scenario: Bank not chosen
- **WHEN** every other Bank field is valid and no bank is chosen
- **THEN** "Add account" is disabled and the hint names the bank

#### Scenario: Several problems at once
- **WHEN** the Loan form has a blank name, no direction chosen and a blank counterparty
- **THEN** "Add account" is disabled and the hint names the name, the direction and the counterparty

#### Scenario: Error clears on edit
- **WHEN** a details field shows an error and the user edits that field
- **THEN** that field's error is cleared

### Requirement: Saved account keeps its details
A successful save SHALL create an account that records the common fields together with the details of its type only, then return the user to My Finances.

#### Scenario: Bank account saved
- **WHEN** the user selects MAIB and kind Savings, enters last 4 digits "0421", name "Salary", amount "2500" in MDL, and saves
- **THEN** a Bank Account named "Salary" is created with 2500 MDL, bank MAIB, kind Savings and last 4 digits "0421", and My Finances is shown
