package com.prodan.taktfinances

import android.app.Application
import com.prodan.taktfinances.di.presentationModule
import com.prodan.taktfinances.di.startTaktOnAndroid

/** Starts the dependency graph once per process, so it outlives activity recreation. */
class TaktApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startTaktOnAndroid(this, listOf(presentationModule))
    }
}
