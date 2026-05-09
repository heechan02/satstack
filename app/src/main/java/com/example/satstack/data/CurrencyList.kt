package com.example.satstack.data

import java.util.Currency

// All ISO 4217 currencies available on the device, sorted by code
val ALL_CURRENCIES: List<Pair<String, String>> =
    Currency.getAvailableCurrencies()
        .sortedBy { it.currencyCode }
        .map { it.currencyCode to it.displayName }

// Returns the currency symbol for a given ISO code (e.g. "KRW" → "₩", "EUR" → "€").
// Falls back to the code itself if the device doesn't recognise it.
fun currencySymbol(code: String): String = try {
    Currency.getInstance(code).symbol
} catch (_: IllegalArgumentException) {
    code
}
