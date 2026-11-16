# Proposal

## Why

The Add Account screen is a plain stack of outlined fields and three dropdowns in a row: the amount looks like any other field, choices that have four to six options hide behind menus, and nothing shows the account being created. The form should feel like a modern finance app and make the amount, the currency, and the account's shape obvious while the user fills it in.

## What Changes

- Live preview card at the top of the screen showing the account as it is entered: bank badge or type icon, name (placeholder "Account name"), last 4 digits, amount, and a summary line such as "Savings · Victoriabank". The amount is the largest element and the currency choice sits next to it.
- Amount input accepts digits and one decimal separator ("." or ","), never more than 2 decimals; while not focused the amount is shown formatted with thousands separators and exactly 2 decimals (`2,500.00`); empty shows a muted `0.00`.
- **BREAKING** Currency choice reduced to MDL, USD, EUR, GBP, RON. CHF and JPY are removed, and every currency uses exactly 2 decimals.
- Account type choice labelled Bank, Investment, Loan, each with an icon.
- Bank choice becomes a horizontally scrolling row of bank badges with Other ("?") last; account kind and investment kind become single-select chips; loan direction stays a two-option toggle.
- Full-width "Add account" button pinned at the bottom, disabled until every required field for the selected type is valid. A live hint above it names what is still missing. This replaces the "report every error on a failed save" behavior.
- A field the user has edited and left shows its error if its content is invalid; untouched fields show no error.
- Same structure on Android (Compose) and iOS (SwiftUI), with platform-native details (Material 3 filter chips with check mark on Android; capsule chips, system segmented control and selection haptics on iOS).

## Capabilities

### New Capabilities
- `finance-account-creation`: opening the Add Account screen, the common fields (type, name, currency, amount), the live preview, amount entry and formatting, save availability with the missing-fields hint, and creating the account. Its requirements were written in the archived `add-finance-account-form` change but never synced into `openspec/specs/`; this change introduces the capability with its current, redesigned behavior.

### Modified Capabilities
- `finance-account-details`: details block uses chips and a bank badge row instead of dropdowns; bank choice wording changes; "Required details block saving" changes from errors on a failed save to a disabled save action plus touched-field errors.

## Impact

- `feature/accounts` domain: `Currency` (drop CHF, JPY, `minorUnitDigits`), `AddAccount` / `parseAmountMinor` (fixed 2 decimals, accept ","), new shared amount input filter and formatter, new shared form validation returning missing fields (used by both the button state and `AddAccount`'s guard); `AddAccountTest`, `DetailOptionsTest` updated.
- `sharedUI` accounts: `AddAccountScreen`, `AddAccountDetails`, `AddAccountState`, `AddAccountIntent`, `AddAccountViewModel`, `BankIcon` (reused for badges); `AddAccountViewModelTest` updated.
- `iosApp/iosApp/Accounts`: `AddAccountView`, `AddAccountModel` rebuilt to the new structure.
- No storage migration: accounts are not persisted yet, so no saved CHF/JPY data exists.
- Assumptions recorded from the design exploration: type labels "Bank / Investment / Loan"; investment kinds wrap onto two rows of chips; the missing-fields hint is a sentence (e.g. "Enter a name and amount, choose a bank and kind").
