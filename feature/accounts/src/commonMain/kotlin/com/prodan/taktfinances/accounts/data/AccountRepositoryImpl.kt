package com.prodan.taktfinances.accounts.data

import app.cash.sqldelight.db.SqlDriver
import com.prodan.taktfinances.accounts.data.db.FinanceAccountQueries
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import com.prodan.taktfinances.accounts.domain.SavedAccount
import com.prodan.taktfinances.core.Outcome
import com.prodan.taktfinances.core.Reason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class AccountRepositoryImpl(
    driver: SqlDriver,
    private val io: CoroutineContext = Dispatchers.IO,
) : AccountRepository {
    private val queries = FinanceAccountQueries(driver)

    override suspend fun add(account: FinanceAccount): Outcome<SavedAccount> = storeCall {
        val id = queries.transactionWithResult {
            val row = account.toRow()
            queries.insert(
                type = row.type,
                name = row.name,
                currency = row.currency,
                amount_minor = row.amount_minor,
                bank = row.bank,
                bank_kind = row.bank_kind,
                last4 = row.last4,
                investment_kind = row.investment_kind,
                platform = row.platform,
                loan_direction = row.loan_direction,
                counterparty = row.counterparty,
            )
            queries.lastInsertRowId().executeAsOne()
        }
        SavedAccount(id, account)
    }

    override suspend fun all(): Outcome<List<SavedAccount>> = storeCall {
        queries.selectAll().executeAsList().map { it.toSavedAccount() }
    }

    /** Runs a store call off the caller's thread; any store or mapping failure is [Reason.Unavailable]. */
    private suspend fun <T : Any> storeCall(block: () -> T): Outcome<T> = try {
        Outcome.Ok(withContext(io) { block() })
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Outcome.Failed(Reason.Unavailable)
    }
}
