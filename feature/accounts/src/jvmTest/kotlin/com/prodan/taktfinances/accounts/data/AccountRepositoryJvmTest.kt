package com.prodan.taktfinances.accounts.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.prodan.taktfinances.accounts.domain.model.AccountType
import com.prodan.taktfinances.accounts.domain.model.Currency
import com.prodan.taktfinances.accounts.domain.model.Money
import com.prodan.taktfinances.accounts.domain.model.NewAccount
import com.prodan.taktfinances.core.Outcome
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AccountRepositoryJvmTest {
    @Test
    fun addThenReadBack() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FinancesDatabase.Schema.create(driver)
        val repository = createAccountRepository(FinancesDatabase(driver))

        val created = repository.add(
            NewAccount(
                name = "Main",
                type = AccountType.Bank,
                balance = Money(minorUnits = 1250L, currency = Currency.Eur),
            ),
        )
        val account = assertIs<Outcome.Ok<*>>(created).value
            as com.prodan.taktfinances.accounts.domain.model.Account

        val listed = repository.accounts()
        val accounts = assertIs<Outcome.Ok<*>>(listed).value
            as List<com.prodan.taktfinances.accounts.domain.model.Account>
        assertEquals(1, accounts.size)
        assertEquals(account.id, accounts.first().id)
        assertEquals("Main", accounts.first().name)
        assertEquals(AccountType.Bank, accounts.first().type)
        assertEquals(Currency.Eur, accounts.first().balance.currency)
        assertEquals(1250L, accounts.first().balance.minorUnits)
    }
}
