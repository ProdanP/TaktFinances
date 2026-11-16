package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.domain.AccountRepository
import org.koin.mp.KoinPlatform

/**
 * The domain types SwiftUI reads from the graph; Swift does not call Koin. Compose screens get
 * their ViewModels with `koinViewModel()` instead. Call [startTakt] first.
 */
class AppDependencies {
    private val koin = KoinPlatform.getKoin()

    val accountRepository: AccountRepository get() = koin.get()
}
