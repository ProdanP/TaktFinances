package com.prodan.taktfinances.accounts

import app.cash.sqldelight.db.SqlDriver
import com.prodan.taktfinances.accounts.data.AccountRepositoryImpl
import com.prodan.taktfinances.accounts.domain.AccountRepository

/** [driver] is the app's one database; the composition root creates its schema. */
fun createAccountRepository(driver: SqlDriver): AccountRepository = AccountRepositoryImpl(driver)
