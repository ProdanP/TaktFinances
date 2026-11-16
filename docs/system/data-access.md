# Data access

Room and Ktor stay in `data`. `:sharedLogic` creates one database and one `HttpClient`. A feature's Koin module receives its DAOs and the client from that graph. It does not open SQLite, build a database, or build a client.

The repository is the boundary. Its public methods return domain types. A call the screen can recover from returns `Outcome` from [errors.md](errors.md). Entities, DAOs, HTTP bodies, and Ktor or SQLite exceptions do not leave `data`.

## Packages

```
<name>/data/
  AccountRepositoryImpl.kt   # repository implementation, internal
  local/
    AccountEntity.kt         # @Entity
    AccountDao.kt            # @Dao
    AccountKindDbo.kt        # storage enum a column uses, one per file
  mapper/
    AccountMapper.kt         # entity ↔ domain, internal
```

- `data.local` contains only Room `@Entity` and `@Dao` types, Room row projections a `@Query` returns, and the storage enums their columns use. No repository, no mapping to domain, no `Outcome`.
- A column with a fixed set of values uses a storage enum in `data.local` (for example `BankKindDbo`), never a domain enum and never a raw `String`. Room stores the entry name as `TEXT`, so entry names are part of the schema: renaming or removing one needs a migration. Keeping them apart from the domain enums means a domain rename or title change never touches stored rows.
- `data.mapper` contains the mappers between storage types and domain types, and nothing else.
- `data` contains the repository implementation. Only `data` and `data.mapper` import `data.local`.
- The feature's Koin module binds the repository implementation; Koin passes it the DAOs that `:sharedLogic` provides.

## Mapping

A new column or JSON field changes `data` only. It changes `domain` when the business rule changed.

Mappers are `internal` extension functions in `data.mapper`, one file per entity (`<Entity>Mapper.kt`, for example `FinanceAccountMapper.kt`). They have two names:

- `toDbo()` maps a domain type to the database object (the `@Entity`): `fun FinanceAccount.toDbo(): FinanceAccountEntity`.
- `toDomain()` maps a database object to a domain type: `fun FinanceAccountEntity.toDomain(): SavedAccount`.

The repository calls only these two functions. It does not build an entity or a domain type field by field. Storage enums map to and from domain enums with exhaustive `when` expressions in the same mapper file, so a new domain entry without a storage entry does not compile. A stored row that can't be mapped back fails: an unknown enum name fails the DAO query, and a missing detail throws inside `toDomain()`. The repository turns both into `Outcome.Failed`.

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
