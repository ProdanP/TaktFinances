package com.prodan.taktfinances.di

/**
 * iOS entry point: `KoinInitializer.shared.doInitKoin()` from iOSApp.swift.
 * The whole graph lives in sharedLogic; Swift takes values from [KoinInjector].
 */
object KoinInitializer {
    fun initKoin() {
        initKoin(config = null)
    }
}
