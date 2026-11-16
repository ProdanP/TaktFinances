package com.prodan.taktfinances.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prodan.taktfinances.accounts.components.AccountDetailContent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AccountDetailScreen(
    accountId: Long,
    checkInOnOpen: Boolean,
    onBack: () -> Unit,
    viewModel: AccountDetailViewModel = koinViewModel(key = "account-$accountId") {
        parametersOf(accountId, checkInOnOpen)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onIntent(AccountDetailIntent.Shown) }
    AccountDetailContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}
