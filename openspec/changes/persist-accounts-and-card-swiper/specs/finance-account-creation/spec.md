# Spec Delta

## MODIFIED Requirements

### Requirement: Create an account
Tapping an enabled "Add account" SHALL create an account with the chosen type, the trimmed name, the chosen currency, the entered amount and the details of its type, store it on the device, then return the user to My Finances with the new account's card selected.

#### Scenario: Successful save
- **WHEN** the user selects Investment, selects EUR, enters name "Brokerage", amount "1500.50", kind Stocks/ETF and platform "XTB", and taps "Add account"
- **THEN** an Investment account named "Brokerage" with amount 1500.50 EUR is created and My Finances is shown with the "Brokerage" card selected

#### Scenario: Cancel
- **WHEN** the user navigates back or cancels without saving
- **THEN** no account is created and My Finances is shown

## ADDED Requirements

### Requirement: Failed save keeps the form
If the account cannot be stored, the Add Account screen SHALL stay open with every entry kept, SHALL show a message that the account could not be saved, and SHALL keep "Add account" enabled so the user can try again. No account SHALL be created.

#### Scenario: Storage fails
- **WHEN** the form is complete and storing the account fails after the user taps "Add account"
- **THEN** the Add Account screen stays open with all entries kept, shows "Couldn't save the account. Try again.", and no account appears on My Finances

#### Scenario: Retry after failure
- **WHEN** the save failure message is shown and the user taps "Add account" again and storing succeeds
- **THEN** the account is created and My Finances is shown with its card selected

### Requirement: Add Account layout on phones
When the space available to the Add Account screen is phone-sized, the screen SHALL show the live preview and the form in one column that scrolls together, with "Add account" and the missing fields hint fixed at the bottom.

#### Scenario: Phone portrait
- **WHEN** the Add Account screen is open on a phone in portrait
- **THEN** the preview is above the form in one scrolling column and "Add account" stays visible at the bottom

### Requirement: Add Account layout on medium widths
When the available width is larger than a phone but too narrow for two columns, the screen SHALL show the single column at a bounded width, centred horizontally, and SHALL NOT stretch fields edge to edge.

#### Scenario: Tablet portrait
- **WHEN** the Add Account screen is open on a tablet in portrait or an open foldable
- **THEN** the preview and form form one centred column that does not span the full width

### Requirement: Add Account layout on wide windows
When the available width fits two columns, the screen SHALL show the live preview in a column beside the form. The preview SHALL stay in view while the form scrolls, and "Add account" with the missing fields hint SHALL sit under the form column.

#### Scenario: Desktop window
- **WHEN** the Add Account screen is open in a wide desktop window or on a tablet in landscape
- **THEN** the preview is shown beside the form, and "Add account" is under the form column

#### Scenario: Preview stays in view
- **WHEN** the user scrolls the form down to the details block on a wide window
- **THEN** the preview is still fully visible and keeps updating as the user edits

### Requirement: Add Account on short windows
When the available height is too short to keep the preview in view, the preview SHALL scroll with the form instead of staying fixed, so the form fields remain reachable.

#### Scenario: Phone landscape
- **WHEN** the Add Account screen is open on a phone in landscape
- **THEN** the preview scrolls away with the form and every field can be reached by scrolling

### Requirement: No double save
While an account is being saved, "Add account" SHALL NOT start a second save.

#### Scenario: Double tap
- **WHEN** the user taps "Add account" twice quickly on a complete form
- **THEN** exactly one account is created
