package com.prodan.taktfinances.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DetailOptionsTest {
    @Test
    fun banksAreTheFiveLargestThenOther() {
        assertEquals(
            listOf("Moldova Agroindbank (MAIB)", "Victoriabank", "Moldindconbank (MICB)", "ProCredit Bank", "Mobiasbancă", "Other"),
            banks().map { it.title },
        )
    }

    @Test
    fun everyBankHasInitialsForItsBadge() {
        assertEquals(true, banks().all { it.initials.isNotBlank() })
    }

    @Test
    fun kindAndDirectionListsMatchTheSpec() {
        assertEquals(listOf("Current", "Savings", "Card", "Deposit"), bankAccountKinds().map { it.title })
        assertEquals(
            listOf("Stocks/ETF", "Crypto", "Bonds", "Deposit", "Real estate", "Other"),
            investmentKinds().map { it.title },
        )
        assertEquals(listOf("Borrowed", "Lent"), loanDirections().map { it.title })
    }
}
