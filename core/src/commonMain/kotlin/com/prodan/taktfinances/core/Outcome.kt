package com.prodan.taktfinances.core

/** Result of a call the screen can recover from. See docs/system/errors.md. */
sealed interface Outcome<out T : Any> {
    data class Ok<T : Any>(val value: T) : Outcome<T>
    data class Failed(val reason: Reason) : Outcome<Nothing>
}

sealed interface Reason {
    data object Unavailable : Reason
}
