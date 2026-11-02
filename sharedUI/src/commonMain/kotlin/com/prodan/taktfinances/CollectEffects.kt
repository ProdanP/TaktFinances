package com.prodan.taktfinances

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun <Effect> CollectEffects(
    effects: Flow<Effect>,
    onEffect: (Effect) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycle) {
        effects.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { effect ->
            currentOnEffect(effect)
        }
    }
}
