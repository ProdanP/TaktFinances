# Tasks

## Tasks

test: ./gradlew :feature:accounts:jvmTest

- [ ] Add the SQLDelight plugin and the Android, native, and JDBC drivers to `docs/system/stack.md` and `gradle/libs.versions.toml`, at a 2.x release that supports this project's Kotlin.
- [ ] Add `:core` with `Outcome` and `Reason` from `docs/system/errors.md`, the same targets as `:sharedLogic`, and register it in `settings.gradle.kts`. `:sharedLogic` depends on it with `api` and exports it.
- [ ] Add `:feature:accounts` with the same targets, and register it. Domain exposes `AccountId`, `AccountType`, `Currency`, `Money`, `Account`, `NewAccount`, `minorUnits`, `accountDraftProblems`, and `AccountRepository`. `:sharedLogic` depends with `api` and exports it. `:sharedUI` depends on it and uses domain types only. `commonTest` covers `minorUnits` and `accountDraftProblems`.
- [ ] Put `account.sq` in `:feature:accounts`. Generate one `FinancesDatabase` in `:sharedLogic` from that schema. Implement `AccountRepository` in `data`, with `createAccountRepository` taking that database. `accounts()` returns newest first and stores `minor_units` as an integer. A `jvmTest` adds one account through an in-memory JDBC driver and reads back the same name, type, currency, and minor units.
- [ ] `:sharedLogic` exposes `createFinancesDatabase(driver)`. Android, desktop, and iOS each create their driver, build the repository, and pass it into `App` or the SwiftUI models. No Koin and no `HttpClient`.
- [ ] Add `AccountsScreen` and `AddAccountScreen` in `:sharedUI` at `com.prodan.taktfinances.accounts`, each with one MVI ViewModel. My Finances replaces the empty mock and keeps Dashboard and Profile as mocks. The shell owns the `add-account` route. The amount field keeps a canonical decimal and shows it with a `VisualTransformation` over `NumberFormat`. The list uses that same formatter. Save stores minor units and returns to the list.
- [ ] Add `AccountsModel`, `AddAccountModel`, and their SwiftUI views in `iosApp/iosApp/Accounts/`. My Finances uses a `NavigationStack`, with + pushing Add Account. Format the field and the rows with `Decimal.FormatStyle.Currency`. Save calls `minorUnits` and pops back to the list.
