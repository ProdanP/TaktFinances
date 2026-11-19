## MODIFIED Requirements

### Requirement: Selection when returning to My Finances
After the user saves a new account, My Finances SHALL show that account's card selected. After the selected account is deleted, My Finances SHALL show the card that followed it selected, or the card before it when it was the last card. When the user returns without saving, the previously selected card SHALL stay selected.

#### Scenario: Land on the new account
- **WHEN** My Finances shows "Salary" selected among two accounts and the user adds an account "Car loan"
- **THEN** My Finances shows "Car loan" selected as the last card

#### Scenario: Cancel keeps the selection
- **WHEN** My Finances shows the second card selected and the user opens Add Account and cancels
- **THEN** the second card is still selected

#### Scenario: Selected account deleted
- **WHEN** the cards are Salary, Brokerage, Car loan with "Brokerage" selected and "Brokerage" is deleted
- **THEN** My Finances shows "Car loan" selected
