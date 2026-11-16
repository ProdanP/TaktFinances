package com.prodan.taktfinances.accounts.domain

import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.MonthlyEntry
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.accounts.domain.entity.StartingPointPayload
import com.prodan.taktfinances.core.Outcome
import kotlinx.datetime.YearMonth

interface AccountRepository {
    /** Stores [account] with its first period started from [start], all or nothing, and returns it with its ids. */
    suspend fun add(account: FinanceAccount, start: StartingPointPayload): Outcome<SavedAccount>

    /** Every stored account with its active period, oldest first. */
    suspend fun all(): Outcome<List<SavedAccount>>

    /** The account with [id] and its active period; `Failed` when it does not exist or can't be read. */
    suspend fun get(id: Long): Outcome<SavedAccount>

    /** Stores [entry] in the Monthly period [periodId], replacing an entry for the same month. */
    suspend fun saveMonthlyEntry(periodId: Long, entry: MonthlyEntry): Outcome<Unit>

    /** Removes the entry for [month] from the Monthly period [periodId]. */
    suspend fun deleteMonthlyEntry(periodId: Long, month: YearMonth): Outcome<Unit>
}
