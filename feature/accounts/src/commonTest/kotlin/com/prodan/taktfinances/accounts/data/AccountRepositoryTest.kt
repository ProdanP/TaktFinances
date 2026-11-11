package com.prodan.taktfinances.accounts.data

import com.prodan.taktfinances.accounts.createAccountRepository
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FinanceAccount
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountRepositoryTest {
    @Test
    fun addedAccountIsStored() {
        val repository = createAccountRepository()
        val account = FinanceAccount(AccountType.Loan, "Car", Currency.MDL, 1000)
        repository.add(account)
        assertEquals(listOf(account), repository.all())
    }
}
