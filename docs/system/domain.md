# Domain

`domain` holds a feature's business types and rules. It is public: `:sharedUI`, `:sharedLogic`, and Swift (through `SharedLogic`) use it.

## Packages

```
<name>/domain/
  entity/
    FinanceAccount.kt        # business model, value type, enum
    StartingPointPayload.kt  # input to a repository call
  error/
    AccountReason.kt         # this feature's failure reasons
  usecase/
    AddAccountUseCase.kt     # one user action with a rule
  AccountRepository.kt       # repository interface
```

- `domain.entity` holds the business models: data classes, value types, enums, and sealed hierarchies. These are not Room `@Entity` types. Those live in `data.local`. See [data-access.md](data-access.md).
- `domain.error` holds this feature's failure types, such as a feature-specific `Reason` passed in `Outcome.Failed`. The shared `Outcome` and `Reason` stay in `:core`. See [errors.md](errors.md). Leave the package out until the feature has its own failure type.
- `domain.usecase` holds use cases. A use case is one class with one public method. Add one only when an action has a rule, a policy, or more than one repository call. See [architecture.md](architecture.md).
- `<Name>Repository.kt` sits directly in `domain`. It is an interface that takes and returns `domain.entity` types and `Outcome`. Its implementation is in `data`.

## Naming

- A business model in `domain.entity` uses the plain domain name: `FinanceAccount`, `AccountType`. No suffix.
- A model that is input to a repository call ends in `Payload`: `StartingPointPayload`. The file name matches: `StartingPointPayload.kt`.
- A use case class ends in `UseCase`, and so does its file: `AddAccountUseCase` in `AddAccountUseCase.kt`.

A pure rule that belongs to one model, such as validation or formatting a value, is a function next to that model in `entity`. It does not need its own use case.

## Dependencies

`domain` depends only on the Kotlin standard library, `kotlinx-datetime` (dates and months) and `:core` (for `Outcome`).

- `domain` never imports `data`, `data.local`, or `data.mapper`. No file under `domain/` imports `com.prodan.taktfinances.<name>.data`.
- `domain` does not import Room, Ktor, Koin, Compose, Android, or platform APIs. A rule that depends on today's date takes it as a `LocalDate` parameter; only the top-level `today()` reads the clock.
- Dependencies point from `data` to `domain`, never the other way. `data` implements the repository interface and maps to and from `domain.entity`. `domain` does not know `data` exists.
