package com.prodan.taktfinances.accounts

sealed interface AccountsIntent {
    data object Opened : AccountsIntent
    data object AddTapped : AccountsIntent
}
