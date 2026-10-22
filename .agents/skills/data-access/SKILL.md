---
name: data-access
description: Keep SQLDelight and Ktor behind a TaktFinances repository. Use when adding or changing a repository implementation, SQLDelight table, query, migration, HTTP call, JSON body, or a failure result.
---

# Data access

Read [docs/system/data-access.md](../../../docs/system/data-access.md) and follow it. Recoverable failures return `Outcome` as in [docs/system/errors.md](../../../docs/system/errors.md). Libraries are fixed in [docs/system/stack.md](../../../docs/system/stack.md).

## Checklist

- [ ] The feature factory takes the shared database and `HttpClient`. It does not construct them.
- [ ] `.sq` files for this feature's tables live in the feature. Query types stay `internal`.
- [ ] The repository returns `Outcome`. It does not throw a failure the screen handles. `CancellationException` is rethrown.
- [ ] Money is `Long` minor units in domain, `INTEGER` in SQL, and a JSON number.
- [ ] A multi-row change that must be atomic uses one `transaction`.
- [ ] No timeout, retry, logging plugin, or cache unless this change needs it.

Do not add Room, a second database, or a second `HttpClient`.
