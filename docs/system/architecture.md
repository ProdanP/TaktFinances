# Architecture

TaktFinances is a Kotlin Multiplatform app. Android and desktop draw with Compose. iOS draws with SwiftUI. Business rules are shared Kotlin. The wizard types `Greeting` and `App` are template code; product code does not go there.

## Modules

| Module | Owns | Must not own |
|---|---|---|
| `:feature:<name>` | One feature's domain and data | Compose, SwiftUI, another feature |
| `:core` | A type two features must change for the same reason | A feature's screens or use cases |
| `:sharedLogic` | Composition root and the `SharedLogic` iOS framework | Feature internals, UI |
| `:sharedUI` | Compose screens and state holders for Android and desktop | Repositories' implementations, SQL, HTTP |
| `:androidApp`, `:desktopApp` | Process entry, window, platform handles passed into the graph | Business rules |
| `iosApp` | SwiftUI screens and the iOS entry | Business rules |

Create `:feature:<name>` with the first real type of that feature. Do not add the module earlier. Create `:core` only when a second feature needs the same rule.

A feature module compiles for the same targets as `:sharedLogic`: Android, `iosArm64`, `iosSimulatorArm64`, and JVM. `:sharedLogic` depends on it with `api`, so the `SharedLogic` framework exports the domain types to Swift. `:sharedUI` depends on the feature module directly.

## Feature layout

```
feature/<name>/src/commonMain/kotlin/com/prodan/taktfinances/<name>/
  domain/
    model/
    AccountRepository.kt      # interface, public
    LoadBalance.kt            # use case, public
  data/
    AccountRepositoryImpl.kt  # internal
```

`data` is `internal`. The module exposes one public factory the composition root calls:

```kotlin
fun createAccountRepository(local: AccountStore): AccountRepository =
    AccountRepositoryImpl(local)
```

Compose for that feature lives in `sharedUI` at `com.prodan.taktfinances.<name>`. SwiftUI for that feature lives in `iosApp/iosApp/<Name>/`. Both call domain types only.

## Dependency rule

Dependencies point inward.

```
Compose / SwiftUI  →  domain  ←  data
```

- `domain` depends on the Kotlin standard library.
- `data` implements domain interfaces.
- A screen or state holder does not import `data`.
- `data` does not import Compose, Android UI, or Swift.
- Platform APIs stay in `androidMain`, `iosMain`, or `jvmMain` and implement an interface declared in `commonMain`.

`:sharedLogic` is the only place that builds the graph. It may use Koin to do that. Shells pass in platform ports (a file path, a database driver). They do not construct repositories themselves, and they do not pass `Context` or Swift types into `domain`. Screens and SwiftUI receive already-built domain types through constructors.

## Orthogonality

A feature does not depend on another feature. Editing accounts does not require editing transactions. The shell connects features by navigation.

When one feature must trigger another, put a narrow interface in `:core` and bind it in `:sharedLogic`. Do not call another feature's repository, data source, or screen.

Inside a feature the same rule applies across layers. A new column or HTTP field changes `data`. It changes `domain` only when the business rule changed.

## DRY

Share code when two call sites are the same rule, so a change to one is a change to the other. Put that rule in `:core`.

Leave two similar functions in place when they change for different reasons. Matching shape is not a reason to merge them.

## KISS

- One use case is one class with one public method, and only when the action has a rule, a policy, or more than one repository call. A read that returns a repository result does not get its own use case; the state holder calls the repository interface.
- Do not add an interface that has one implementation, except a domain boundary (repository, platform port) or a fake in tests.
- Do not add a Gradle module for a type used in one place.
- A state holder stores screen state and calls domain types. It does not open a database or build an HTTP request.
- Prefer a direct function over a new type.

## Patterns

Use these. Skip a pattern that has one call site.

| Pattern | Use when |
|---|---|
| Repository | A feature reads or writes something that outlives the screen. Interface in `domain`, implementation in `data`. |
| Use case | One user action coordinates a rule or several repository calls. |
| Adapter | A platform API must appear as a `commonMain` port. The source set is the adapter. |
| Strategy | A rule varies and the composition root should pick the variant. One interface, small implementations. An `if` with two branches is enough otherwise. |
| Composition root | Creating implementations. That is `:sharedLogic`, not a screen and not a feature's domain. Koin is allowed only there. |

Do not resolve dependencies from Koin outside `:sharedLogic`. Do not add a base class per feature. Features share structure by copying the layout above, not by inheriting from a common feature type.

## Tests

- Use-case tests go in the feature's `commonTest`. They use a fake repository.
- Repository tests use a fake store or driver, not a running device.
- Business rules are not tested through Compose or SwiftUI.
