package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.data.AccountRepositoryImpl
import com.prodan.taktfinances.accounts.data.local.FinanceAccountDao
import com.prodan.taktfinances.accounts.domain.AccountRepository
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/** [dao] comes from the app's one database, which the composition root opens. */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Only the composition root calls this.
fun createAccountRepository(dao: FinanceAccountDao): AccountRepository = AccountRepositoryImpl(dao)
