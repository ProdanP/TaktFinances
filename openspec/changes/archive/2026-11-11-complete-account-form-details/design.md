## Context

`FinanceAccount(type, name, currency, amountMinor)` is flat, and `AddAccount(type, name, currency, amount)` validates only name and amount, returning `Invalid(nameInvalid, amountInvalid)`. The form is written twice: Compose in `sharedUI` (`AddAccountState`/`Intent`/`ViewModel`/`Screen`) and SwiftUI in `iosApp/iosApp/Accounts/`. Swift already reads results by casting (`result as? AddAccountResultInvalid`). The repository is in-memory, there are no `.sq` files, and `sharedUI` has no bank assets. Validation lives in the domain use case so the rule is not duplicated per platform (see presentation.md and architecture.md). See proposal.md for scope and the spec for behavior.

## Goals / Non-Goals

**Goals:**
- Keep every validation rule in the shared `AddAccount` use case.
- Make the new inputs easy to drive from Swift without constructing Kotlin sealed types.
- Keep one `FinanceAccount` value that cannot hold details from the wrong type.

**Non-Goals:**
- Persistence, an account list, and any money logic that applies the loan direction.
- Real bank logos and localization of the new labels.

## Decisions

1. **Stored shape: common fields plus a sealed `AccountDetails`.** `FinanceAccount(name, currency, amountMinor, details)` with `AccountDetails` = `Bank(bank, kind, last4?)`, `Investment(kind, platform)`, `Loan(direction, counterparty)`. The type is derived from the details (`val type`), so type and details cannot disagree. Alternative: keep `type` and add nullable fields to the flat class, rejected because it permits a Loan with a bank and forces every consumer to null-check.

2. **Input shape: a flat draft, not the sealed type.** `AddAccount` receives `type`, name, currency, amount, and an `AccountDetailsDraft` holding every optional input (`bank`, `bankAccountKind`, `last4`, `investmentKind`, `platform`, `loanDirection`, `counterparty`). The use case reads only the fields for `type` and builds the sealed `AccountDetails`. Swift and the ViewModel then only fill plain fields, and the "which fields apply" rule exists once. Alternative: UI builds `AccountDetails` directly, rejected because incomplete input (no bank chosen yet) has no valid sealed value and Swift would need to construct Kotlin subclasses.

3. **Reporting invalid fields.** `AddAccountResult.Invalid` becomes name, amount, and a `DetailErrors` data class of booleans (`bank`, `accountKind`, `last4`, `investmentKind`, `platform`, `direction`, `counterparty`). All problems are reported in one attempt, as the spec requires. Booleans over a `Set` of an error enum because Swift reads plain Bool properties without collection bridging. Concrete result types stay so Swift can keep casting as it does now.

4. **Enums for fixed choices.** `Bank` (MAIB, Victoriabank, MICB, ProCredit, Mobiasbanca, Other), `BankAccountKind`, `InvestmentKind`, `LoanDirection`, each with a display `title`. The domain exposes ordered lists (like `accountTypes()`), so both UIs render the same options in the same order. "Other" bank is a plain entry with no name.

5. **Bank icons as a monogram badge.** The domain `Bank` carries only data the UI needs to draw a badge: display initials and a brand-like color value as a plain ARGB `Long`, no Compose types in `domain`. A `BankIcon` composable in `sharedUI` draws a colored circle with the initials. SwiftUI draws the same badge from the same values. A logo swap later means replacing the composable and adding resources, not changing the domain. Alternative: bundle logo files now, rejected because no assets are available and iOS would need a second copy of them.

6. **Last 4 digits.** Validated in the use case as `\d{4}` after trimming, empty meaning absent. The UI additionally filters to digits and caps input at 4 characters, which satisfies the "cannot type a full number" scenario. The use case still rejects anything else, so the rule holds even if a UI filter is missed.

7. **Switching type.** The ViewModel and the Swift model reset the draft and its errors on `TypeSelected` while keeping name, currency and amount. Alternative: keep per-type drafts in memory so switching back restores them, rejected as surprising behavior the spec does not ask for.

8. **Compose form.** Add a details section composable per type under the amount field. Bank and kind use the existing `ExposedDropdownMenuBox` pattern (as `CurrencyField`); loan direction and account kind may use segmented buttons where the count fits (2 and 4 options respectively fit; Investment kind has 6 so it uses the dropdown). Details inputs live on `AddAccountState` as one `AccountDetailsDraft` plus `DetailErrors`.

9. **iOS form.** `AddAccountModel` mirrors the new state and intents; `AddAccountView` adds a per-type `Section` using `Picker` (menu or segmented) and `TextField`. Kotlin enums arrive as Swift classes, as the existing `AccountType` and `Currency` already do.

10. **Tests.** Business rules are tested only in `commonTest` of `:feature:accounts` (docs/system/architecture.md). ViewModel tests cover type-switch reset, error clearing, and effect emission. No Compose or SwiftUI rule tests.

## Risks / Trade-offs

- [Swift cannot read a sealed `AccountDetails` ergonomically] → Nothing in the form reads it. Only the future list will, and it can use `as?` casts as the result type does now.
- [Flat draft carries unused fields] → The use case ignores fields that do not belong to the type, and the draft is cleared on type switch, so stale values never reach a saved account.
- [Bank ranking and brand colors may be disputed] → The list is a decision recorded in the proposal and is easy to edit in one enum.
- [Last 4 digits resembles a PIN] → Label it "Last 4 digits of account or card" and use a numeric keyboard.
- [Two UI implementations drift] → Both follow the same enum lists and `DetailErrors` names, and the spec scenarios are the checklist for manual verification on both.
- [Existing tests and call sites break on the new signature] → Updated in the domain group of tasks before UI work starts.
