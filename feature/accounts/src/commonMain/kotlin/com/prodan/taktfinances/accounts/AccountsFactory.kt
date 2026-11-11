package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.data.AccountRepositoryImpl
import com.prodan.taktfinances.accounts.domain.AccountRepository

fun createAccountRepository(): AccountRepository = AccountRepositoryImpl()
