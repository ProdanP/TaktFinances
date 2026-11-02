package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.AccountsViewModel
import com.prodan.taktfinances.accounts.AddAccountViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Presentation module lives in sharedUI, not in sharedLogic:
 * ViewModels are platform-owned for Compose.
 */
val uiModule = module {
    viewModelOf(::AccountsViewModel)
    viewModelOf(::AddAccountViewModel)
}
