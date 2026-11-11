package com.prodan.taktfinances.accounts

sealed interface AddAccountEffect {
    data object Saved : AddAccountEffect
}
