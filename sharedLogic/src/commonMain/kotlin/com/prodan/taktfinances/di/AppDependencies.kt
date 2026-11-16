package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.domain.AccountRepository
import org.koin.mp.KoinPlatform

/**
 * The only place that resolves from Koin. Compose and SwiftUI read these properties and pass
 * them into screens through constructors; screens never call Koin themselves. Koin stays an
 * implementation detail of :sharedLogic. Call [startTakt] first.
 */
class AppDependencies {
    private val koin = KoinPlatform.getKoin()

    val accountRepository: AccountRepository get() = koin.get()
}
