package com.prodan.taktfinances.accounts

sealed interface AccountsEffect {
    data object OpenAddAccount : AccountsEffect
}
