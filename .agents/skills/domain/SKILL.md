---
name: domain
description: Shape a TaktFinances feature's domain layer. Use when adding or changing a domain model, value type, enum, use case, repository interface, or feature-specific failure reason.
---

# Domain

Read [docs/system/domain.md](../../../docs/system/domain.md) and follow it. Module placement is in [docs/system/architecture.md](../../../docs/system/architecture.md). Failures follow [docs/system/errors.md](../../../docs/system/errors.md).

## Checklist

- [ ] Business models, value types, and enums go in `domain/entity/`. These are not Room `@Entity` types; those go in `data/local/`.
- [ ] This feature's failure types go in `domain/error/`. Shared `Outcome` and `Reason` stay in `:core`.
- [ ] Use cases go in `domain/usecase/`. Each is one class with one public method, added only when the action has a rule, a policy, or more than one repository call.
- [ ] The repository interface is `domain/<Name>Repository.kt`. It takes and returns `domain.entity` types and `Outcome`.
- [ ] Nothing under `domain/` imports the feature's `data` packages (`data`, `data.local`, `data.mapper`).
- [ ] `domain` imports only the Kotlin standard library and `:core`. No Room, Ktor, Koin, coroutines, Compose, or platform APIs.
- [ ] A use case with a rule has a `commonTest`.
