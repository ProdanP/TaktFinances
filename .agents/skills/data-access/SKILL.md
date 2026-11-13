---
name: data-access
description: Keep Room and Ktor behind a TaktFinances repository. Use when adding or changing a repository implementation, Room entity, DAO, query, migration, HTTP call, JSON body, or a failure result.
---

# Data access

Read [docs/system/data-access.md](../../../docs/system/data-access.md) and follow it. Recoverable failures return `Outcome` as in [docs/system/errors.md](../../../docs/system/errors.md). Libraries are fixed in [docs/system/stack.md](../../../docs/system/stack.md).

## Checklist

- [ ] The feature factory takes its DAOs and the shared `HttpClient`. It does not build a database or a client.
- [ ] This feature's `@Entity` and `@Dao` types live in its `data.local` package (`<name>/data/local/`), marked `@HiddenFromObjC`. `data.local` holds nothing else.
- [ ] The repository implementation and the entity ↔ domain mapping stay in `data`. Only `data` imports `data.local`. New entities are added to `AppDatabase` in `:sharedLogic` with a version bump, and the exported schema in `sharedLogic/schemas/` is committed.
- [ ] The repository returns `Outcome`. It does not throw a failure the screen handles. `CancellationException` is rethrown.
- [ ] Money is `Long` minor units in domain, `INTEGER` in SQL, and a JSON number.
- [ ] A multi-row change that must be atomic uses one transaction (`@Transaction` DAO function or `immediateTransaction`).
- [ ] No timeout, retry, logging plugin, or cache unless this change needs it.

Do not add SQLDelight, a second database, or a second `HttpClient`.
