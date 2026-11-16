package com.prodan.taktfinances.core

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Runs a store or network [block] in a repository and returns its value as [Outcome.Ok]. Any
 * exception becomes [Reason.Unavailable], except [CancellationException], which is rethrown so
 * cancellation still propagates. Inline, so [block] may call suspend functions from a suspend caller.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // A data-layer helper; Swift only receives Outcome.
inline fun <T : Any> outcomeOf(block: () -> T): Outcome<T> = try {
    Outcome.Ok(block())
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    Outcome.Failed(Reason.Unavailable)
}
