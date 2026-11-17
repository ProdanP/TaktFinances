package com.prodan.taktfinances.accounts

internal sealed interface AddAccountEffect {
    data object Saved : AddAccountEffect
}
