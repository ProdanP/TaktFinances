package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.data.FinancesDatabase
import com.prodan.taktfinances.accounts.data.createAccountRepository
import com.prodan.taktfinances.accounts.domain.AccountRepository
import org.koin.dsl.module

/**
 * Data and domain graph. SqlDriver comes from [platformModule].
 * Presentation ViewModels are registered in `:sharedUI`.
 */
internal fun commonModule() = module {
    single { FinancesDatabase(get()) }
    single<AccountRepository> { createAccountRepository(get()) }
}
