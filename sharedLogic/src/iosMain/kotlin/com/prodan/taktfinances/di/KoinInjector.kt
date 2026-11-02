package com.prodan.taktfinances.di

import com.prodan.taktfinances.accounts.domain.AccountRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Swift-side access to the graph: `KoinInjector.shared.accountRepository`. */
object KoinInjector : KoinComponent {
    val accountRepository: AccountRepository by inject()
}