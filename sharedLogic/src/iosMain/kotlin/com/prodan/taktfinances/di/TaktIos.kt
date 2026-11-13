package com.prodan.taktfinances.di

/**
 * Entry for Swift (`TaktIosKt.startTaktOnIos()`), which does not see SQLDelight types: opens the
 * app database and starts the dependency graph.
 */
fun startTaktOnIos() = startTakt(nativeDatabaseDriver())
