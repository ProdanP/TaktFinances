package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.model.AccountId
import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency
import com.prodan.taktfinances.accounts.domain.model.Money
import com.prodan.taktfinances.accounts.domain.model.NewAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.prodan.taktfinances.accounts.domain.model.Account as DomainAccount

internal class AccountRepositoryImpl(
    private val db: FinancesDatabase,
) : AccountRepository {
    override suspend fun accounts(): Outcome<List<DomainAccount>> {
        return try {
            val rows = db.accountQueries.selectAllNewestFirst().executeAsList()
            Outcome.Ok(rows.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Outcome.Failed(Reason.Unavailable)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun add(account: NewAccount): Outcome<DomainAccount> {
        return try {
            val id = AccountId(Uuid.random().toString())
            db.accountQueries.insert(
                id = id.value,
                name = account.name,
                type = account.type.name,
                currency = account.balance.currency.code,
                minor_units = account.balance.minorUnits,
            )
            Outcome.Ok(
                DomainAccount(
                    id = id,
                    name = account.name,
                    type = account.type,
                    balance = account.balance,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Outcome.Failed(Reason.Unavailable)
        }
    }
}

private fun Account.toDomain(): DomainAccount =
    DomainAccount(
        id = AccountId(id),
        name = name,
        type = AccountType.valueOf(type),
        balance = Money(
            minorUnits = minor_units,
            currency = Currency.entries.first { it.code == currency },
        ),
    )

fun createAccountRepository(database: FinancesDatabase): AccountRepository =
    AccountRepositoryImpl(database)
