# Data access

Room and Ktor stay in `data`. `:sharedLogic` creates one database and one `HttpClient`. A feature receives its DAOs and the client through its factory. It does not open SQLite, build a database, or build a client.

The repository is the boundary. Its public methods return domain types. A call the screen can recover from returns `Outcome` from [errors.md](errors.md). Entities, DAOs, HTTP bodies, and Ktor or SQLite exceptions do not leave `data`.

## Packages

```
<name>/data/
  AccountRepositoryImpl.kt   # repository implementation, internal
  AccountRows.kt             # entity ↔ domain mapping, internal
  local/
    AccountEntity.kt         # @Entity
    AccountDao.kt            # @Dao
```

- `data.local` contains only Room `@Entity` and `@Dao` types (and Room row projections a `@Query` returns). No repository, no mapping to domain, no `Outcome`.
- `data` contains the repository implementation and the mapping between entities and domain types. It is the only package that imports `data.local`.
- The factory takes the DAOs from `data.local` and passes them to the repository implementation.

## Mapping

A new column or JSON field changes `data` only. It changes `domain` when the business rule changed.

Money is a `Long` of minor units in domain, in SQL (`INTEGER`), and in JSON (a number). Do not use `REAL`, `Double`, or a decimal string.

```kotlin
internal class AccountRepositoryImpl(
    private val dao: AccountDao,
    private val http: HttpClient,
) : AccountRepository {
    override suspend fun balance(id: AccountId): Outcome<Money> {
        val row = try {
            dao.balance(id.value) // @Query suspend fun balance(id: Long): AccountBalanceRow?
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return Outcome.Failed(Reason.Unavailable)
        }
        val minorUnits = row?.minorUnits ?: return Outcome.Failed(Reason.Unavailable)
        return Outcome.Ok(Money(minorUnits = minorUnits))
    }
}
```

The DAO and `HttpClient` stay inside this class. A missing row is `Reason.Unavailable` unless the screen treats "missing" differently from a failed read.

## Writes

Several writes that must succeed or fail together run in one transaction: a `@Transaction` DAO function, or `useWriterConnection { it.immediateTransaction { ... } }`. Do not catch a failure and continue with the next write.

## What stays out

Do not put timeouts, retries, logging, or a cache in a feature until that feature needs them. Do not add a second database or a second `HttpClient`.
