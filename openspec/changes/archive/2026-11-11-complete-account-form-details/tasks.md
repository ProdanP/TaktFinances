# Tasks

## 1. Domain model and validation (`:feature:accounts`)

- [x] 1.1 Add `Bank` (MAIB, Victoriabank, MICB, ProCredit, Mobiasbanca, Other, each with title, initials and ARGB color), `BankAccountKind`, `InvestmentKind` and `LoanDirection` enums with ordered list functions; verify with a test that each list has the options and order the spec names
- [x] 1.2 Add sealed `AccountDetails` (`Bank`, `Investment`, `Loan`) and change `FinanceAccount` to `(name, currency, amountMinor, details)` with a derived `type`; update `AccountRepositoryImpl` and `AccountRepositoryTest`, and verify the module compiles and the repository test passes
- [x] 1.3 Add `AccountDetailsDraft` and `DetailErrors`, extend `AddAccount` and `AddAccountResult.Invalid` to validate required details per type (trim platform and counterparty, last 4 as exactly 4 digits or empty), and report all invalid fields in one result; verify with `AddAccountTest` cases for each spec scenario (valid bank, last 4 omitted, "12" / "12a4" / "123", blank platform, blank counterparty, negative loan amount, several errors at once, details of the other types ignored)
- [x] 1.4 Run `./gradlew :feature:accounts:allTests` and confirm it is green

## 2. Compose form (`:sharedUI`)

- [x] 2.1 Extend `AddAccountState` and `AddAccountIntent` with the details draft, `DetailErrors`, and intents for each details field; update `AddAccountViewModel` so type selection keeps name, currency and amount but resets the draft and details errors, edits clear that field's error, and save passes the draft to `AddAccount`; verify with `AddAccountViewModelTest` for type-switch reset, error clearing, last 4 capped at 4 digits, and `Saved` effect on success
- [x] 2.2 Add a `BankIcon` composable drawing the monogram badge, and bank, kind, direction, investment kind, platform, counterparty and last 4 digits fields as per-type details sections in `AddAccountScreen`; verify by running the desktop app and checking the three type layouts and the bank list with icons against the spec scenarios
- [x] 2.3 Verify error display for every details field on Compose, including a save that reports name and several details errors together, and that the form scrolls on a short window

## 3. iOS form (`iosApp`)

- [x] 3.1 Extend `AddAccountModel.swift` with the same state, intents and reset-on-type-change rules, reading `DetailErrors` from the result; verify the iOS app builds against the updated `SharedLogic` framework
- [x] 3.2 Add the per-type details sections and a SwiftUI bank badge to `AddAccountView.swift`; verify on an iPhone simulator that each type shows its own fields, the bank list shows badges, last 4 digits accepts at most 4 digits, and a save with missing details shows the errors

## 4. Integration

- [x] 4.1 Walk through the spec scenarios end to end on Android or desktop and on iOS, including the full bank save example, and confirm both platforms behave the same
- [x] 4.2 Run `./gradlew allTests` and confirm no regressions in other modules
