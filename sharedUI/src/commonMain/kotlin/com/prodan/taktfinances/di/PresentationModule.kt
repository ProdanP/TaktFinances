package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.AccountDetailViewModel
import com.prodan.taktfinances.accounts.AddAccountViewModel
import com.prodan.taktfinances.home.HomeViewModel
import com.prodan.taktfinances.onboarding.LandingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The Compose ViewModels. The Android and desktop shells pass this to their `startTaktOn*` entry. */
val presentationModule: Module = module {
    viewModelOf(::LandingViewModel)
    viewModel { HomeViewModel(get()) }
    viewModel { AddAccountViewModel(get()) }
    viewModel { (accountId: Long, checkInOnOpen: Boolean) ->
        AccountDetailViewModel(accountId, checkInOnOpen, get(), get(), get())
    }
}
