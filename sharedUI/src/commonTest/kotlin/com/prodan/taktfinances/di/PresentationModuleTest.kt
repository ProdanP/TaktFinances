package com.prodan.taktfinances.di

import com.prodan.taktfinances.FakeAccountRepository
import com.prodan.taktfinances.accounts.AccountDetailViewModel
import com.prodan.taktfinances.accounts.AddAccountViewModel
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.usecase.AddAccountUseCase
import com.prodan.taktfinances.accounts.domain.usecase.CheckInUseCase
import com.prodan.taktfinances.accounts.domain.usecase.DeleteCheckInUseCase
import com.prodan.taktfinances.home.HomeViewModel
import com.prodan.taktfinances.onboarding.LandingViewModel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class PresentationModuleTest {
    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun everyViewModelResolves() {
        val koin = startKoin {
            val accounts = module {
                single<AccountRepository> { FakeAccountRepository() }
                factoryOf(::AddAccountUseCase)
                factoryOf(::CheckInUseCase)
                factoryOf(::DeleteCheckInUseCase)
            }
            modules(accounts, presentationModule)
        }.koin
        assertNotNull(koin.get<LandingViewModel>())
        assertNotNull(koin.get<HomeViewModel>())
        assertNotNull(koin.get<AddAccountViewModel>())
        assertNotNull(koin.get<AccountDetailViewModel> { parametersOf(1L, true) })
    }
}
