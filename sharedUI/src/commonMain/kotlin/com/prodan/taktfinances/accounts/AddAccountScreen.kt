package com.prodan.taktfinances.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.accounts.components.AddAccountContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddAccountScreen(
    onClose: () -> Unit,
    viewModel: AddAccountViewModel = koinViewModel(),
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
