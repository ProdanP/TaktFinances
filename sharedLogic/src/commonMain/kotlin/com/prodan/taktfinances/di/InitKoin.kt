package com.prodan.taktfinances.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Entry point for every platform. Android calls it from Application.onCreate,
 * Desktop from main(), iOS via [KoinInitializer].
 */
fun initKoin(
    config: (KoinApplication.() -> Unit)? = null,
) {
    startKoin {
        config?.invoke(this)
        modules(platformModule(), commonModule())
    }
}
