package com.prodan.taktfinances.accounts.domain.entity

/**
 * The account to select once [removedId] is gone: the first one after it, or else the last one before it.
 * Ids grow in card order, so [remaining] alone is enough; null when no account is left.
 */
fun neighbourAfterRemoving(remaining: List<SavedAccount>, removedId: Long): SavedAccount? =
    remaining.filter { it.id > removedId }.minByOrNull { it.id }
        ?: remaining.filter { it.id < removedId }.maxByOrNull { it.id }
