package com.prodan.taktfinances

import android.app.Application
import com.prodan.taktfinances.di.initKoin
import com.prodan.taktfinances.di.uiModule
import org.koin.android.ext.koin.androidContext

class FinancesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FinancesApplication)
            modules(uiModule)
        }
    }
}
