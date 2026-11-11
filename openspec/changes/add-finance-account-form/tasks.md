# Tasks

## 1. Accounts feature module

- [x] 1.1 Create `:feature:accounts` (Android, iosArm64, iosSimulatorArm64, JVM like `:feature:home`), register in `settings.gradle.kts`, add `api` + iOS `export` in `:sharedLogic`, dependency in `:sharedUI`; verify `./gradlew :feature:accounts:build :sharedLogic:build` succeeds
- [x] 1.2 Add domain types `AccountType`, `Currency` (with minor-unit digits), `FinanceAccount`, `AccountRepository`, `AddAccountResult`, and `AddAccount` use case (trim name, parse amount to minor units, reject blank name / invalid or negative amount); verify with `commonTest` covering valid input, blank name, "abc", negative, and over-precise amounts, and JPY rejecting decimals, using a fake repository
- [x] 1.3 Add internal in-memory `AccountRepositoryImpl` and public `createAccountRepository()`; verify a `commonTest` that an added account is stored

## 2. Android and desktop UI

- [x] 2.1 Add `AddAccountState/Intent/Effect/ViewModel` in `sharedUI` `accounts` package and a ViewModel unit test (`commonTest`) for field edits (including currency), validation errors clearing on edit, and the `Saved` effect; verify `./gradlew :sharedUI:allTests` passes
- [ ] 2.2 Add `AddAccountScreen` (segmented type picker, currency dropdown defaulting to MDL, name, amount, Save, back) with a width-bounded layout and `CollectEffects`; verify it renders on a phone-width and wide window via preview or running the desktop app
- [ ] 2.3 Add `HomeEffect`/`AddAccountTapped` to the Home ViewModel, the My Finances-only top bar with "+" in compact/medium/expanded layouts, and the `add-account` route in `App.kt`; verify tapping "+" opens the form and Save/back returns to My Finances (`./gradlew :desktopApp:run`, and Android build)
- [x] 2.4 Update docs (`docs/system/architecture.md` module table if needed, `docs/learnings.md` if anything surprising) and verify links/names match the code

## 3. iOS UI

- [x] 3.1 Add `Accounts/AddAccountModel.swift` and `AddAccountView.swift` (segmented type `Picker`, currency menu `Picker`, name and amount fields, Save) calling `AddAccount` from `SharedLogic`; verify the iOS app builds
- [ ] 3.2 Introduce `NavigationStack` in `ContentView`, My Finances toolbar "+" in `HomeView` (compact and regular layouts), and navigation to the form; verify on iPhone and iPad simulators that "+" opens the form and Save returns to My Finances

## 4. Integration

- [ ] 4.1 Walk the spec scenarios on Android, desktop, and iOS (add action only on My Finances, defaults including MDL, currency choice, each validation error, successful save, cancel) and run `openspec validate add-finance-account-form --strict`
