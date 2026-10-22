---
name: architecture
description: Place a change inside a TaktFinances feature module using clean architecture. Use when adding a feature, screen, use case, repository, or library, or when deciding which Gradle module a type belongs in.
---

# Architecture

Read [docs/system/architecture.md](../../../docs/system/architecture.md) before adding or moving code. Follow it. The rules below are the decision steps. A new library goes in [docs/system/stack.md](../../../docs/system/stack.md) before it goes in `gradle/libs.versions.toml`.

## Where a type goes

1. Business rule or stored fact → `:feature:<name>`, in `domain` or `data`. A repository, query, or HTTP call follows [docs/system/data-access.md](../../../docs/system/data-access.md). A recoverable failure returns `Outcome` as in [docs/system/errors.md](../../../docs/system/errors.md).
2. Compose screen or state holder → `:sharedUI`, package `com.prodan.taktfinances.<name>`. Follow [docs/system/presentation.md](../../../docs/system/presentation.md): one MVI ViewModel for Android and desktop.
3. SwiftUI screen → `iosApp/iosApp/<Name>/`, with one `@Observable` screen model.
4. Wiring implementations together → `:sharedLogic`.
5. Process entry or a platform handle → `:androidApp`, `:desktopApp`, or `iosApp`.
6. The same rule in two features → `:core`. One feature's helper stays in that feature.

## Add a feature

- [ ] `:feature:<name>` Kotlin Multiplatform module with Android, `iosArm64`, `iosSimulatorArm64`, and JVM, matching `:sharedLogic`
- [ ] Register it in `settings.gradle.kts`
- [ ] `domain` public, `data` internal, one public factory
- [ ] `:sharedLogic` depends on it with `api`
- [ ] `:sharedUI` depends on the module and uses domain types only
- [ ] No dependency from this feature onto another `:feature:` module
- [ ] A `commonTest` for each use case that has a rule

## Patterns

Use a repository for anything stored. Use a use case when an action has a rule or calls more than one repository. Use an adapter in a platform source set for a platform API. Use strategy only when `:sharedLogic` must choose among variants. A single call site stays a direct function. Libraries for storage, HTTP, the graph, and navigation are fixed in [docs/system/stack.md](../../../docs/system/stack.md).
