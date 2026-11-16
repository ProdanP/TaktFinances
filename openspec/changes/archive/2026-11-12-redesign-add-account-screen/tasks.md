# Tasks

## 1. Shared amount and currency rules (`:feature:accounts`)

- [x] 1.1 Reduce `Currency` to MDL, USD, EUR, GBP, RON with only `code`, remove `minorUnitDigits`, and add the `AMOUNT_DECIMALS = 2` constant; verify with a `DetailOptionsTest` case that `currencies()` returns exactly those 5 in order with MDL as default
- [x] 1.2 Add `filterAmountInput` (digits plus one separator, "," → ".", max 2 decimals, leading separator → "0.") and change `parseAmountMinor` to take only the text, use 2 decimals, and accept a trailing separator; verify with tests for "2500,5", "3.999" → "3.99", "-12a.5" → "12.5", "1.2.3" → "1.23", ".5" → "0.5", "12." → 1200, and blank → null
- [x] 1.3 Add `formatAmount(minor)` and `formatAmountInput(text)`; verify with tests for "2500" → "2,500.00", "2500.5" → "2,500.50", "1234567.8" → "1,234,567.80", "0" → "0.00", and empty → null

## 2. Shared form check and hint (`:feature:accounts`)

- [x] 2.1 Add `FormField` (form order) and `checkAccountForm(type, name, amount, details)` returning the invalid fields of the selected type, `canSave` and the ordered missing list; verify with tests for an empty form per type, a complete form per type, last 4 "12" invalid but empty valid, and fields of other types ignored
- [x] 2.2 Add `missingFieldsHint(fields)`; verify with tests for the Bank empty case ("Enter a name and amount, choose a bank and kind"), a details-only case, each Loan and Investment field, and an empty list → null
- [x] 2.3 Make `AddAccount` use `checkAccountForm`, change `AddAccountResult.Invalid` to carry `invalid: Set<FormField>`, remove `DetailErrors`, and replace the JPY test in `AddAccountTest` with fixed-2-decimal cases; verify all existing `AddAccountTest` save scenarios still pass with the new result shape
- [x] 2.4 Run `./gradlew :feature:accounts:allTests` and confirm it is green

## 3. Compose state and ViewModel (`:sharedUI`)

- [x] 3.1 Update `AddAccountState` and `AddAccountIntent`: add `FieldFocusChanged(field, focused)`, `focused`, `left`, and derived `visibleErrors`, `canSave`, `hint` and `amountText`; remove `nameInvalid`, `amountInvalid` and `detailErrors`; verify the module compiles
- [x] 3.2 Update `AddAccountViewModel` to filter amount input, track focus and the edited-and-left fields (chip and badge taps count as left), clear details entries in `left` on type change, and save only when `canSave`; verify with `AddAccountViewModelTest` cases for: disabled on open, enabled when complete, disabled after clearing the name, hint shrinking while filling, no errors before leaving a field, name error after clearing and leaving, error cleared on edit, raw amount while focused vs "2,500.50" after, type switch disabling save, and the `Saved` effect

## 4. Compose screen (`:sharedUI`)

- [x] 4.1 Build the preview card composable (type icon or `BankIcon`, name or placeholder, last 4, large amount `BasicTextField` with "0.00" placeholder, currency pill with `DropdownMenu`, summary line); verify with a Compose `@Preview` for the empty and Bank filled states
- [x] 4.2 Replace the type segmented row with an icon `ButtonGroup` of Bank, Investment, Loan (segmented row fallback); verify in the preview that labels don't truncate at 360dp width
- [x] 4.3 Rebuild `AddAccountDetails`: bank `LazyRow` of badges with selection ring and Other last, `FilterChip` + `FlowRow` for account kind and investment kind, the loan direction toggle, and last 4 with "••••" prefix; errors driven by `visibleErrors`; verify with an Investment filled `@Preview` showing all 6 kind chips on 2 rows
- [x] 4.4 Pin the hint and full-width "Add account" button in the `Scaffold` bottom bar with `imePadding`, disabled from `canSave`, with the hint as the button's accessibility description while disabled; verify on the Android emulator that the button stays above the keyboard and the 3 states match the previews, and capture screenshots

## 5. iOS screen (`iosApp`)

- [x] 5.1 Update `AddAccountModel.swift` with the same focus and edited-and-left tracking, amount filtering, and derived `visibleErrors`, `canSave`, `hint`, `amountText` from the shared functions, and drop `DetailErrorFlags`; verify the iOS app builds against the updated `SharedLogic` framework
- [x] 5.2 Rebuild `AddAccountView.swift` as `ScrollView` + `VStack` with the preview card, currency `Menu`, segmented type picker, horizontal bank badge row, capsule chips in a `FlowLayout` with selection haptics, and errors from `visibleErrors`; add `#Preview`s for the empty, Bank filled and Investment filled states and verify they render
- [x] 5.3 Add the bottom `safeAreaInset` with the hint and the disabled-until-valid "Add account" capsule button on `.ultraThinMaterial`, and a "Cancel" toolbar button; verify on an iPhone simulator that the 3 states match the previews, the button stays above the keyboard, and capture screenshots

## 6. Integration

- [x] 6.1 Walk through the spec scenarios of `finance-account-creation` and `finance-account-details` on Android (or desktop) and iOS, including the full Bank save example and the comma separator, and confirm both platforms behave the same
- [x] 6.2 Run `./gradlew allTests` and confirm no regressions in other modules
