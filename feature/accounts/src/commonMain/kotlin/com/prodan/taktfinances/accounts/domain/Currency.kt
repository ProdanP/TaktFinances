package com.prodan.taktfinances.accounts.domain

enum class Currency(val code: String) {
    MDL("MDL"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    RON("RON"),
}

/** Every supported currency records amounts with exactly this many decimal places. */
const val AMOUNT_DECIMALS: Int = 2

fun currencies(): List<Currency> = Currency.entries

fun defaultCurrency(): Currency = Currency.MDL
