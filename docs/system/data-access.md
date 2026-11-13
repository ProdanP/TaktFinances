# Data access

Room and Ktor stay in `data`. `:sharedLogic` creates one database and one `HttpClient`. A feature receives its DAOs and the client through its factory. It does not open SQLite, build a database, or build a client.

The repository is the boundary. Its public methods return domain types. A call the screen can recover from returns `Outcome` from [errors.md](errors.md). Entities, DAOs, HTTP bodies, and Ktor or SQLite exceptions do not leave `data`.

## Packages

```
<name>/data/
  AccountRepositoryImpl.kt   # repository implementation, internal
  local/
    AccountEntity.kt         # @Entity
    AccountDao.kt            # @Dao
  mapper/
    AccountMapper.kt         # entity ↔ domain, internal
```

- `data.local` contains only Room `@Entity` and `@Dao` types (and Room row projections a `@Query` returns). No repository, no mapping to domain, no `Outcome`.
- `data.mapper` contains the mappers between storage types and domain types, and nothing else.
- `data` contains the repository implementation. Only `data` and `data.mapper` import `data.local`.
- The factory takes the DAOs from `data.local` and passes them to the repository implementation.

## Mapping

A new column or JSON field changes `data` only. It changes `domain` when the business rule changed.

Mappers are `internal` extension functions in `data.mapper`, one file per entity (`<Entity>Mapper.kt`, for example `FinanceAccountMapper.kt`). They have two names:

- `toDbo()` maps a domain type to the database object (the `@Entity`): `fun FinanceAccount.toDbo(): FinanceAccountEntity`.
- `toDomain()` maps a database object to a domain type: `fun FinanceAccountEntity.toDomain(): SavedAccount`.

The repository calls only these two functions. It does not build an entity or a domain type field by field. A stored row that can't be mapped back, such as an unknown code or a missing detail, throws inside `toDomain()`, and the repository turns that into `Outcome.Failed`.

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
        row ?: return Outcome.Failed(Reason.Unavailable)
        return Outcome.Ok(row.toDomain()) // data.mapper: fun AccountBalanceRow.toDomain(): Money
    }
}
```

The DAO and `HttpClient` stay inside this class. A missing row is `Reason.Unavailable` unless the screen treats "missing" differently from a failed read.

## Writes

Several writes that must succeed or fail together run in one transaction: a `@Transaction` DAO function, or `useWriterConnection { it.immediateTransaction { ... } }`. Do not catch a failure and continue with the next write.

## What stays out

Do not put timeouts, retries, logging, or a cache in a feature until that feature needs them. Do not add a second database or a second `HttpClient`.
