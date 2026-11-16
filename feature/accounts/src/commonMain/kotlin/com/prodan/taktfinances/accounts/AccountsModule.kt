package com.prodan.taktfinances.accounts

import com.prodan.taktfinances.accounts.data.AccountRepositoryImpl
import com.prodan.taktfinances.accounts.domain.AccountRepository
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The accounts bindings. The composition root includes this module and supplies
 * [com.prodan.taktfinances.accounts.data.local.FinanceAccountDao] from the app's one database.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Only the composition root includes this.
val accountsModule: Module = module {
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
}
