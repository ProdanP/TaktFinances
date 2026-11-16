package com.prodan.taktfinances.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.accounts.components.AddAccountContent
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.usecase.AddAccount

@Composable
fun AddAccountScreen(
    repository: AccountRepository,
    onClose: () -> Unit,
    viewModel: AddAccountViewModel = viewModel { AddAccountViewModel(AddAccount(repository)) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            AddAccountEffect.Saved -> onClose()
        }
    }
    AddAccountContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onClose,
    )
}
