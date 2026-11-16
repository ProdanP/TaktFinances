# Design

## Context

See proposal.md for motivation and specs/ for the required behavior.

Current state:
- Validation lives only in `AddAccount` (`feature/accounts/domain`) and runs when the user saves. It returns `AddAccountResult.Invalid(nameInvalid, amountInvalid, detailErrors)`, and both state holders copy those flags into state.
- `parseAmountMinor` reads `Currency.minorUnitDigits` and accepts only "." as the separator.
- Android and desktop use the MVI `AddAccountViewModel` in `:sharedUI`. iOS has its own `@Observable` `AddAccountModel` that calls the same domain types through `SharedLogic` (docs/system/presentation.md). Each platform builds its own screen, so every shared rule must sit in `feature/accounts` to stay the same on both.
- Compose Multiplatform 1.12.1 with material3 `1.12.0-alpha03`; iOS deployment target is 18.2, so the iOS 26 glass APIs are not available.
- Accounts are held in memory only (no SQLDelight tables yet).

## Goals / Non-Goals

**Goals:**
- One shared source for amount filtering, amount formatting, form validity, the ordered list of missing fields and the hint sentence, used by both state holders and by `AddAccount`.
- Screen state stays in the state holder: focus and "edited and left" tracking go through intents, not Compose `remember` or SwiftUI `@State`.
- Same screen structure on both platforms with native controls.

**Non-Goals:**
- Real bank logos (monogram badges stay; `BankIcon` and `BankBadge` remain the swap points).
- Locale-aware number formatting; the format is fixed to `,` thousands and `.` decimals.
- Dark theme tuning beyond using the existing color scheme roles.
- Storage of accounts.

## Decisions

### 1. Shared form rules in `feature/accounts/domain`
Add a form-rules file in the domain package:
- `enum class FormField` in form order: `Name, Amount, Bank, AccountKind, Last4, InvestmentKind, Platform, Direction, Counterparty`.
- `fun checkAccountForm(type, name, amount, details): FormCheck`, where `FormCheck` holds `invalid: Set<FormField>` (fields of the selected type that are missing or invalid) and derives `canSave`, `missing: List<FormField>` (form order) and `hint`.
- `fun missingFieldsHint(fields: List<FormField>): String?` that builds the sentence ("Enter a name and amount, choose a bank and kind"), with null for an empty list. It keeps form order and groups neighbouring fields that share a verb, so a Loan reads "Enter a name, choose a direction, enter a counterparty".

`AddAccount` calls `checkAccountForm` instead of its private `buildDetails` error logic, and `AddAccountResult.Invalid` carries `invalid: Set<FormField>`. `DetailErrors` is removed.

The hint text sits in the domain module because iOS cannot reach `:sharedUI`, and the user asked for one wording on both platforms. Alternative: build the sentence separately in Compose and SwiftUI. Rejected because the two would drift.

### 2. Amount rules: fixed 2 decimals
- `Currency` becomes `MDL, USD, EUR, GBP, RON` with only `code`. `AMOUNT_DECIMALS = 2` is a domain constant.
- `fun filterAmountInput(raw: String): String` keeps digits and the first separator, maps "," to ".", drops anything after 2 decimals, and turns a leading separator into "0.". Both state holders run it on `AmountChanged`, as they already do for last 4 digits.
- `parseAmountMinor(text)` drops the currency parameter, accepts a trailing separator ("12." → 1200), and still rejects blank.
- `fun formatAmount(minor: Long): String` gives "2,500.00", and `fun formatAmountInput(text): String?` formats the raw input for display, returning null when the input is empty.

Alternative: keep `minorUnitDigits` for future currencies. Rejected because the user removed JPY and CHF. A currency without decimals can bring the field back when it is needed.

### 3. Focus and "edited and left" go through state
New intents on both platforms: `FieldFocusChanged(field: FormField, focused: Boolean)`. State gains `focused: FormField?`, `edited: Set<FormField>` (text edited since it last lost focus) and `left: Set<FormField>` (a field moves from `edited` to `left` when it loses focus). A text edit removes the field from `left`, which clears its error until the user leaves it again. Chip and badge selections go straight into `left` on tap.

Focus events can arrive in either order (the next field may report focus before the previous one reports losing it), so a field joins `left` whenever it reports losing focus, whatever `focused` holds at that moment. `AddAccountViewModelTest` covers this order.

Each state holder recomputes `FormCheck` after every intent and exposes derived values: `visibleErrors = check.invalid ∩ left`, `canSave`, `hint`, `amountText` (raw while `focused == Amount`, else `formatAmountInput`). `nameInvalid`, `amountInvalid` and `detailErrors` leave state. `TypeSelected` clears the details-related entries in `left`.

Alternative: keep focus in Compose `remember`. Rejected by docs/system/presentation.md (no screen `remember` state), and it would make the raw-versus-formatted rule untestable in `AddAccountViewModelTest`.

### 4. Android (Compose) structure
- `Scaffold` with a `TopAppBar` (close icon, "New account") and a `bottomBar` that holds the hint text and a full-width `Button` (`enabled = state.canSave`). `windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))` keeps the button above the keyboard and the gesture bar. While disabled, the button carries the hint as its `stateDescription`.
- Preview card: a `primary`-colored `Surface` with a 24dp corner. The amount is a `BasicTextField` styled `displaySmall`, with value `state.amountText` and a muted "0.00" placeholder. Focus changes come through `onFocusChanged` and go out as intents. The currency is an `AssistChip`-style pill in `primaryContainer` that opens a `DropdownMenu`. The menu's `expanded` flag stays a local `remember`, as the current currency and bank dropdowns already do: it is transient widget state, not screen state.
- Type choice: a `Row` of material3 expressive `ToggleButton`s using `ButtonGroupDefaults.connected*ButtonShapes()`, with icons (`AccountBalance`, `TrendingUp`, `SwapHoriz`). In this alpha, `ButtonGroup` itself is built around an overflow menu, so the connected-shapes row is the simpler fit. Only the checked button shows its icon, and it widens (weight 1.3 against 1, animated). That keeps "Investment" from being cut off at 360dp, which happened on the emulator with an icon on every button.
- Bank row: a `LazyRow` of 76dp items, each a 44dp `BankIcon` with `Bank.shortName` under it. On phones the 5th badge is cut off at the edge, which shows the row scrolls. The selected badge gets a 2.5dp `primary` ring, and each item is `selectable` with the radio-button role.
- Chips: `FilterChip` with a check `leadingIcon` when selected, inside a `FlowRow`.
- Loan direction keeps the 2-option segmented row.
- Errors: `isError` and `supportingText` driven by `visibleErrors`. Chip groups show red label and helper text.

### 5. iOS (SwiftUI) structure
- Replace `Form` with `ScrollView` + `VStack` of labelled groups on the `FinancesPalette` background, so the preview card and chip rows lay out freely.
- Same preview card, with `TextField` amount bound through `send` and `@FocusState` mirrored into the model with `FieldFocusChanged` (SwiftUI requires `@FocusState` in the view, and the model stays the source of truth for which field was left). The currency pill is a `Menu`.
- Type choice: system segmented `Picker`. Bank row: `ScrollView(.horizontal)` with `.scrollTargetBehavior(.viewAligned)`.
- Chips: capsule `Button`s in a small `FlowLayout` (SwiftUI `Layout` protocol), with `.sensoryFeedback(.selection, trigger:)` on change.
- Pinned button: `.safeAreaInset(edge: .bottom)` holding the hint and a `.borderedProminent` capsule button with `.disabled(!canSave)`, on `.ultraThinMaterial`. "Cancel" is a toolbar text button that replaces the back button.
- Filtered fields (amount and last 4 digits) use `FilteredTextField`. A plain `TextField(text:)` keeps showing what was typed when the binding's setter stores a different value: on the simulator, "2500,5" stayed on screen while the model held "2500.5". `FilteredTextField` keeps a display copy in local `@State`, sends each edit through the binding, then reads the binding back and shows the stored value. That also covers a rejected keystroke ("3.999" stays "3.99"), where the stored value doesn't change and no change event fires. The model stays the source of truth.
- Autocorrect is off for every text field except Name. Platforms and counterparties are proper names, and the keyboard offered to replace "Binance".
- The view has a second `init(model:)` so the `#Preview`s can build their states with `send` instead of a special preview state.

### 6. Previews for visual checks
Add Compose `@Preview`s of `AddAccountContent` for empty, Bank filled and Investment filled states, and SwiftUI `#Preview`s for the same 3 states. These replace the HTML mockups as the visual reference and give the simulator and emulator screenshots for review.

### 7. Shared display text for the preview
The preview summary and bank labels must read the same on both platforms, so they live in the domain next to the hint, for the same reason as decision 1:
- `fun accountSummary(type, details): String?` builds "Savings · Victoriabank", "Crypto · Binance" or "Lent · Ana" from the details of the selected type, with null when nothing is chosen.
- `Bank.shortName` ("MAIB", "MICB", "ProCredit", …) labels the badges and the summary. `Bank.title` keeps the full legal name.
- `AccountType.title` for bank accounts becomes "Bank" (from "Bank Account"), matching the type labels in the spec. Only the Add Account screens used it.

## Risks / Trade-offs

- [Disabled button hides why it is disabled] → the hint is always visible while disabled and is the button's accessibility description. Errors appear on fields the user has left.
- [Hint sentence too long on small phones when all 4 Bank fields are missing] → the hint wraps to 2 lines at most. `missingFieldsHint` groups "Enter …" and "choose …" to stay short, and is unit-tested for each type.
- [material3 alpha `ToggleButton` / connected-shapes API changes] → isolated in the `TypeToggle` composable. `SingleChoiceSegmentedButtonRow` with icons is the fallback.
- [Simulator text injection drops characters] → when the simulator injects a whole string at once into a filtered iOS field, some characters are lost while it re-renders. Typing one key at a time works, so this looks specific to the simulator, but it hasn't been checked on a physical iPhone.
- [`@FocusState` and model focus drift on iOS] → the view only reports focus changes and never reads focus back from the model, so the 2 can't fight.
- [Removing CHF/JPY breaks callers] → only `AddAccountTest` uses JPY. No stored data exists.
- [Amount typed with a leading separator or trailing "."] → normalized by `filterAmountInput` and accepted by `parseAmountMinor`, with tests for both.

## Migration Plan

No data migration: accounts are not persisted. Ship both platforms in one change, because the shared domain API (`Currency`, `AddAccountResult`, `DetailErrors` removal) changes for both state holders at once. Rollback is a revert of the change.
