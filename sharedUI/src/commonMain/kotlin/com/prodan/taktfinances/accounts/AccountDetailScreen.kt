package com.prodan.taktfinances.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodan.taktfinances.accounts.components.AccountDetailContent
import com.prodan.taktfinances.accounts.domain.AccountRepository

@Composable
fun AccountDetailScreen(
    accountId: Long,
    checkInOnOpen: Boolean,
    repository: AccountRepository,
    onBack: () -> Unit,
    viewModel: AccountDetailViewModel = viewModel(key = "account-$accountId") {
        AccountDetailViewModel(accountId, checkInOnOpen, repository)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onIntent(AccountDetailIntent.Shown) }
    AccountDetailContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}
