# Data access

SQLDelight and Ktor stay in `data`. `:sharedLogic` creates one database and one `HttpClient`. A feature receives them through its factory. It does not open SQLite or build a client.

The repository is the boundary. Its public methods return domain types. A call the screen can recover from returns `Outcome` from [errors.md](errors.md). Generated query types, HTTP bodies, and Ktor or SQLite exceptions do not leave `data`.

## Mapping

A new column or JSON field changes `data` only. It changes `domain` when the business rule changed.

Money is a `Long` of minor units in domain, in SQL (`INTEGER`), and in JSON (a number). Do not use `REAL`, `Double`, or a decimal string.

```kotlin
internal class AccountRepositoryImpl(
    private val db: AppDatabase,
    private val http: HttpClient,
) : AccountRepository {
    override suspend fun balance(id: AccountId): Outcome<Money> {
        val row = try {
            db.accountQueries.balance(id.value).executeAsOneOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return Outcome.Failed(Reason.Unavailable)
        }
        val minorUnits = row?.minor_units ?: return Outcome.Failed(Reason.Unavailable)
        return Outcome.Ok(Money(minorUnits = minorUnits))
    }
}
```

`executeAsOneOrNull` and `HttpClient` stay inside this class. A missing row is `Reason.Unavailable` unless the screen treats "missing" differently from a failed read.

## Writes

Several writes that must succeed or fail together run in one SQLDelight `transaction`. Do not catch a failure and continue with the next write.

## What stays out

Do not put timeouts, retries, logging, or a cache in a feature until that feature needs them. Do not add a second database or a second `HttpClient`.
