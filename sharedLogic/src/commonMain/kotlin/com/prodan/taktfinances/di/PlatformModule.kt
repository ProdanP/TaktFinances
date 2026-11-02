package com.prodan.taktfinances.di

import org.koin.core.module.Module

/**
 * Platform-specific bindings — today only the SQLDelight [app.cash.sqldelight.db.SqlDriver].
 */
expect fun platformModule(): Module
