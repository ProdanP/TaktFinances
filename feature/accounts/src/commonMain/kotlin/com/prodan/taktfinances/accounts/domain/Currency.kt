package com.prodan.taktfinances.accounts.domain

enum class Currency(val code: String, val minorUnitDigits: Int) {
    MDL("MDL", 2),
    USD("USD", 2),
    EUR("EUR", 2),
    GBP("GBP", 2),
    RON("RON", 2),
    CHF("CHF", 2),
    JPY("JPY", 0),
}

fun currencies(): List<Currency> = Currency.entries

fun defaultCurrency(): Currency = Currency.MDL
