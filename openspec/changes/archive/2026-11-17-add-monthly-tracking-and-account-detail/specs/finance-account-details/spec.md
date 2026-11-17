# Spec Delta

## MODIFIED Requirements

### Requirement: Investment details
For an Investment the form SHALL require an investment kind of Stocks/ETF, Crypto, Bonds, Deposit, Real estate or Other, offered as single-select chips that wrap onto more than one line when needed, with none preselected, and SHALL require a non-blank platform entered as free text. The platform SHALL be trimmed before saving.

#### Scenario: All kinds visible
- **WHEN** the Investment form is shown on a phone
- **THEN** all six investment kind chips are visible without scrolling sideways

#### Scenario: Valid investment
- **WHEN** the user selects kind Crypto, enters platform "  Binance " and saves with a valid name
- **THEN** an Investment account is created with kind Crypto and platform "Binance"

#### Scenario: Blank platform
- **WHEN** the platform is blank
- **THEN** "Add account" is disabled and the hint names the platform
