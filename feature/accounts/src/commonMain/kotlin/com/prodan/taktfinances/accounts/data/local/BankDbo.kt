package com.prodan.taktfinances.accounts.data.local

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Stored value of the `bank` column. Room stores the entry name as TEXT, so names are part of the
 * schema: never rename or remove an entry without a migration. Mapped to the domain enum in data.mapper.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC // Swift never sees storage types.
enum class BankDbo { MAIB, VICTORIABANK, MICB, PROCREDIT, MOBIASBANCA, OTHER }
