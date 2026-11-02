# Design

## Approach

`:feature:accounts` is a new Kotlin Multiplatform module with the same targets as `:sharedLogic`. Register it in `settings.gradle.kts`. `:core` is created in the same bolt because this is the first repository that can fail. `Outcome` and `Reason` live there, as in `docs/system/errors.md`. `:sharedLogic` depends on both with `api` and exports them in the `SharedLogic` framework. `:sharedUI` depends on `:feature:accounts` and uses domain types only.

Domain types:

```kotlin
enum class AccountType { Bank, Investment }

enum class Currency { Eur, Usd, Mdl }

data class Money(val minorUnits: Long, val currency: Currency)

data class Account(
    val id: AccountId,
    val name: String,
    val type: AccountType,
    val balance: Money,
)
```

`AccountId` is a value class around a `String`. Euro, US dollar, and Moldovan leu each have scale 2. Changing currency keeps the same `minorUnits`. It does not convert between currencies. An amount of `0` is valid. A negative amount is rejected.

Locale formatting stays on the platform. Domain only turns the field’s canonical text into minor units:

```kotlin
fun minorUnits(canonical: String): Long?
```

`canonical` is digits and at most one `.`, with at most two digits after it. `12.5` is `1250`. An empty string, a minus sign, a second separator, or a third fraction digit returns `null`. No grouping characters and no currency symbol enter this function.

Compose keeps that canonical string on `AddAccountState`. The amount `TextField` displays it with a `VisualTransformation` that uses `java.text.NumberFormat` for the selected currency and the device locale: symbol, grouping, decimal separator, symbol position, and two fraction digits. The transformation’s offset map writes keystrokes back into the canonical string. Android and desktop share that transformation in `:sharedUI`. The account list is plain text, so it uses the same `NumberFormat` on the stored minor units. `VisualTransformation` applies only to a text field.

iOS uses `Decimal.FormatStyle.Currency` for the amount field and for each list row, with the account currency and the device locale. SwiftUI has no `VisualTransformation`. The model converts the parsed `Decimal` through `minorUnits` before save.

A direct function `accountDraftProblems(name, type, currency, minorUnits)` returns the problems that block save: blank name after trim, missing type, missing currency, missing amount. It is not a use case. Zero is not a problem.

`AccountRepository` is the domain interface. `data` implements it and is `internal`. There is no use case: the list reads one repository call, and save validates then calls `add` once. The public factory takes the shared database only. This feature makes no HTTP call, so it does not take an `HttpClient`.

```kotlin
interface AccountRepository {
    suspend fun accounts(): Outcome<List<Account>>
    suspend fun add(account: NewAccount): Outcome<Account>
}
```

`NewAccount` is the trimmed name, type, and `Money`. The data layer assigns the id. SQLDelight is added to the version catalog at a 2.x release that supports this project’s Kotlin. One database, `FinancesDatabase`, is generated in `:sharedLogic`. The `account` `.sq` file lives in `:feature:accounts`. `:sharedLogic` declares the database and depends on that module for the schema. Drivers are created in the shells and passed in: Android driver, native driver on iOS, JDBC driver on desktop. The table is:

```sql
CREATE TABLE account (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  type TEXT NOT NULL,
  currency TEXT NOT NULL,
  minor_units INTEGER NOT NULL
);
```

`type` is `Bank` or `Investment`. `currency` is `EUR`, `USD`, or `MDL`. `minor_units` is an `INTEGER`. `accounts()` returns newest first. Failures return `Outcome.Failed(Reason.Unavailable)` and rethrow `CancellationException`.

`:sharedLogic` exposes `createFinancesDatabase(driver)` and `createAccountRepository(database)`. The Android and desktop shells construct the driver and pass the repository into `App`. The iOS entry does the same and passes the repository into the SwiftUI models. Koin is not added. One repository does not need a graph.

Compose keeps the existing `landing` and `home` routes and adds `add-account`. The shell owns that route. `HomeScreen` still switches Dashboard, My Finances, and Profile. Dashboard and Profile stay title mocks. The My Finances body is `AccountsScreen`. `App` passes `onNavigateToAddAccount` through `HomeScreen` into that screen. `AccountsViewModel` loads on `Opened` and sends `OpenAddAccount` from the + button. `CollectEffects` calls the shell callback. The + button is the top end of a bar titled My Finances, on every width. The list column is centered and bounded at 420 dp. Each row shows the account name, Bank or Investment, and the platform currency format of that balance. No rows means an empty list under the bar.

`AddAccountScreen` has its own `AddAccountViewModel`. The type control lists only Bank and Investment, with nothing selected. Choosing a type reveals name, currency, and current amount. Currency lists only Euro, US dollar, and Moldovan leu, with nothing selected. The amount field’s state is the canonical string. The `VisualTransformation` shows the locale currency format, and changing currency keeps the canonical string so the same amount is shown in the new currency. Save calls `minorUnits` and then `accountDraftProblems`. Any problem stays on `State` and the screen stays put. A clean draft calls `add`. Success sends a `Saved` effect. The shell pops back to Home. `AccountsScreen` loads again on `Opened`, so the new row is visible. A failed save sets the unavailable error on `State`.

iOS uses `@Observable` `AccountsModel` and `AddAccountModel` in `iosApp/iosApp/Accounts/`. My Finances, on compact and regular width, is a `NavigationStack`. The + button is the navigation-bar trailing item. Pushing Add Account is stack state on the accounts model. Save pops that stack. The models call the shared repository, `minorUnits`, and `accountDraftProblems`. Display formatting stays in SwiftUI. Layout still follows `horizontalSizeClass` only for the Home chrome that already exists. The accounts column stays readable on a wide detail pane.

`commonTest` covers `minorUnits` (pad one fraction digit, reject a third fraction digit, empty, and negative) and `accountDraftProblems`. It does not retest locale separators or symbols. A `jvmTest` uses an in-memory JDBC driver, adds one account, and reads back the same name, type, currency, and minor units.

## Non-goals

- Editing or deleting an account
- A second account type or a fourth currency
- Converting an amount when the currency changes
- Negative balances
- Transactions, transfers, or a combined total across currencies
- A locale picker, or a shared model of grouping characters and symbol placement. The platform currency formatter owns that
- Dashboard or Profile content
- Network sync, Koin, or a second database
- An empty-state illustration

## Acceptance criteria

- My Finances lists each saved account with its name, Bank or Investment, and the amount in the platform currency format for that currency and the device locale, with two decimal places.
- With no saved accounts, My Finances shows the title and the + button, and no account rows.
- The + button at the top right opens Add Account on Android, desktop, and iOS, and back returns to My Finances.
- Add Account offers only Bank and Investment. The name, currency, and amount fields appear after a type is chosen. Bank and Investment use the same fields.
- Currency offers only Euro, US dollar, and Moldovan leu. Changing it shows the same canonical amount in the new currency’s platform format.
- As the person types a major-unit amount, Compose shows it through a `VisualTransformation`, and iOS through `Decimal.FormatStyle.Currency`. The field’s stored text stays a canonical decimal. Save stores `minor_units` as an integer. The formatted text is not stored.
- A blank name, a missing currency, a missing amount, or a negative amount does not save. Zero does save.
- After a successful save, My Finances shows the new account, newest first.
- Accounts domain and SQLDelight schema live in `:feature:accounts`. `Outcome` lives in `:core`. Compose lives in `:sharedUI` at `com.prodan.taktfinances.accounts`. SwiftUI lives in `iosApp/iosApp/Accounts/`. The shell owns the Add Account route.

## Figma

none
