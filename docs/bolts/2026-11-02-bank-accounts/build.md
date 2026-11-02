# Build

## Notes

Implemented My Finances accounts end to end. SQLDelight 2.4.1 is in the catalog. `:core` holds `Outcome`. `:feature:accounts` owns domain, `account.sq`, `FinancesDatabase`, and the repository. Generating the database in the feature that owns the schema avoids a cycle with `:sharedLogic`; the composition root still exposes `createFinancesDatabase` and `createAccountRepository`. `AccountId` is a data class so common code compiles without `@JvmInline`.

Koin follows the kmp-clean-template shape: `initKoin` plus `platformModule` / `commonModule` in `:sharedLogic`, Compose `uiModule` with `viewModelOf` in `:sharedUI`, screens via `koinViewModel()`, iOS `KoinInitializer` / `KoinInjector`. Compose uses a `VisualTransformation` and `NumberFormat` for the amount field and list. SwiftUI uses `Decimal.FormatStyle.Currency`. `./gradlew :feature:accounts:jvmTest` passed.
