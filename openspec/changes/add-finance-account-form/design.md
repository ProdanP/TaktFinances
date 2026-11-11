# Design

## Context

Home is a three-destination shell (`HomeScreen` Compose, `HomeView` SwiftUI) showing placeholder text per tab. Routes live in `App.kt` (`landing`, `home`); screens take navigation callbacks and collect ViewModel effects with `CollectEffects` (see docs/system/presentation.md). iOS has `ContentView` switching Landing/Home with a bool and no `NavigationStack`. No storage, no `:core`, no Koin exist yet. See proposal.md for scope.

## Goals / Non-Goals

**Goals:** follow the existing module/MVI conventions; same behavior on Android, desktop, and iOS; adaptive layout (bounded-width form on wide windows).

**Non-Goals:** account list, persistence, currency, edit/delete, a `:core` module.

## Decisions

1. **New `:feature:accounts` module** (`domain` public, `data` internal, one factory), per architecture.md. Contents: `AccountType` enum (BankAccount, Investment, Loan), `Currency` enum (MDL, USD, EUR, GBP, RON, CHF, JPY; each with its minor-unit digits), `FinanceAccount(type, name, currency, amount)`, `AccountRepository.add(account)`, and `AddAccount` use case, which owns the validation rule (trim name, parse amount). Use case returns a small sealed `AddAccountResult` (`Created`, `Invalid(nameError, amountError)`), a concrete type so Swift can switch without casting. Alternative: validate in ViewModel/Swift model — rejected, it would duplicate the rule on two platforms.
2. **Amount as `Long` minor units inside the domain, parsed from a decimal string** with up to the chosen currency's fraction digits (2 for most, 0 for JPY), so the use case takes `currency` and `amount` text together, avoiding floating point for money. Alternative: `Double` — rejected for rounding. Loan amount is the amount owed, entered as a positive number; sign semantics are deferred.
3. **In-memory repository**, no `Outcome`/`:core` yet: `add` cannot fail, and the architecture adds `Outcome` with the first failing repository. When SQLDelight arrives, the interface gains `Outcome` in that change. Accounts therefore vanish on app restart (accepted, stated in proposal).
4. **Repository wiring**: no Koin exists, so the shell creates one `createAccountRepository()` and passes it into the screen/ViewModel factory (Compose `App`, iOS `ContentView`), keeping a single instance for the session so a later list reads the same data.
5. **Compose navigation**: add route `add-account` in `App.kt`. `HomeScreen` takes `onAddAccount: () -> Unit`; `HomeViewModel` emits `HomeEffect.OpenAddAccount` on `AddAccountTapped`, collected via `CollectEffects`. The My Finances top bar is a Material3 `TopAppBar` with an `IconButton` (Add icon, content description "Add account") shown only when `state.selected == MyFinances`, placed in the content area of all three layouts (bottom bar, rail, drawer). `AddAccountScreen(onClose)` has its own ViewModel (`AddAccountState/Intent/Effect`), `SingleChoiceSegmentedButtonRow` for type, two `OutlinedTextField`s (decimal keyboard for amount), Save button, back arrow. Save success sends `AddAccountEffect.Saved` → `onClose()`.
6. **iOS**: wrap `HomeView` in a `NavigationStack` in `ContentView` with a path state; My Finances content gets a `.toolbar` "+" `ToolbarItem` that sends `.addAccountTapped` to `HomeModel`, which sets a state flag/destination the stack shows. `AddAccountView` with a `Form` (`Picker` segmented, two `TextField`s, Save) and an `@Observable AddAccountModel` calling `AddAccount` from `SharedLogic`. `:sharedLogic` gets `api` + framework `export` of `:feature:accounts`.
7. **Currency as a fixed enum**, not a free-text ISO code or a platform locale API: it keeps the rule in shared `domain`, works identically on all three platforms, and a short list is enough for a minimal form. Compose uses an `ExposedDropdownMenuBox` and iOS a menu `Picker`. Alternative: full ISO 4217 list — deferred; extend the enum when needed.
8. **Strings** are literals in the UI like existing screens; no localization resource system exists yet.

## Risks / Trade-offs

- [Data lost on restart] → Stated limitation; persistence follow-up.
- [Saved accounts not visible] → Users see only return navigation; mitigated by the next change (list). Optionally show a short confirmation effect if desired later.
- [iOS `HomeView` uses split view on regular width, toolbar placement differs] → Put the toolbar on the detail/My Finances content in both layouts and verify on iPhone and iPad simulators.
- [Short currency list] → Enum is easy to extend; other currencies are not selectable yet.
- [Money parsing locale] → Accept only `.` as decimal separator now; locale-aware input deferred.
