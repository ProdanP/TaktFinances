## Why

The Add Account form records every account with the same four fields (type, name, currency, amount), so a bank account, an investment and a loan are indistinguishable beyond their label. A bank account has no bank, a loan has no counterparty and no direction (borrowed or lent), and an investment has no kind or platform. Capturing these now gives later list, net-worth and reporting work something real to build on.

## What Changes

- Add a type-specific details block to the Add Account form, shown below the common fields and switching with the selected type:
  - **Bank Account**: bank (required), account kind (required), last 4 digits (optional).
  - **Investment**: investment kind (required), platform (required, free text).
  - **Loan**: direction, Borrowed or Lent (required), counterparty (required, free text).
- Bank choice is MAIB, Victoriabank, MICB, ProCredit Bank, Mobiasbancă, or Other. Each bank shows an icon: a colored monogram badge drawn in code, with room to replace it with real logos later. "Other" is a plain entry with no free-text name.
- Account kinds are Current, Savings, Card, Deposit. Investment kinds are Stocks/ETF, Crypto, Bonds, Deposit, Real estate, Other.
- The last 4 digits accept exactly 4 digits when present, so a full account or card number can never be stored.
- Loan amounts stay non-negative. Direction is a separate field, and no sign is applied to the stored amount.
- Replace the flat `FinanceAccount` with common fields plus a sealed, type-specific details value. `AddAccount` takes the details and validates them.
- Switching type keeps name, currency and amount, and clears the previous type's details.
- Mirror the form changes in the SwiftUI Add Account screen.

Out of scope: dates, interest rates, repayment schedules, include-in-total toggle, a free-text name for "Other" bank, real bank logo assets, persistence, an account list.

## Capabilities

### New Capabilities
- `finance-account-details`: the type-specific details a finance account records (bank, kind, last 4, platform, direction, counterparty), their options and validation, and how the Add Account form presents them.

### Modified Capabilities
<!-- None. The earlier add-finance-account-form change was archived without syncing, so openspec/specs has no finance-account-creation spec to modify. -->

## Impact

- `feature/accounts` domain: new bank, account kind, investment kind and loan direction types; `FinanceAccount` gains a details value; `AddAccount` and `AddAccountResult.Invalid` gain details validation; `AccountRepositoryImpl` is unaffected beyond the type change.
- `sharedUI` accounts: `AddAccountState`, `AddAccountIntent`, `AddAccountViewModel`, `AddAccountScreen` (Compose) gain the details block and a bank icon composable.
- `iosApp` Accounts: `AddAccountModel.swift` and `AddAccountView.swift` gain the same block. Sealed Kotlin types must be readable from Swift.
- Tests: `AddAccountTest`, `AddAccountViewModelTest` and `AccountRepositoryTest` are updated for the new shape.
- Behaviour dependency: the form fields requirement from `add-finance-account-form` (type, name, currency, amount) stays as it was and is not restated here.
