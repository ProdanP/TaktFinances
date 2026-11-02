package com.prodan.taktfinances.accounts

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.prodan.taktfinances.accounts.domain.minorUnits
import com.prodan.taktfinances.accounts.domain.model.Currency

class CurrencyAmountVisualTransformation(
    private val currency: Currency?,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val canonical = text.text
        val selected = currency ?: return TransformedText(text, OffsetMapping.Identity)
        val units = minorUnits(canonical)
        if (units == null) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val formatted = formatMoney(units, selected)
        return TransformedText(
            AnnotatedString(formatted),
            DigitOffsetMapping(canonical, formatted),
        )
    }
}

private class DigitOffsetMapping(
    private val original: String,
    private val transformed: String,
) : OffsetMapping {
    private val originalDigits = digitIndexes(original)
    private val transformedDigits = digitIndexes(transformed)

    override fun originalToTransformed(offset: Int): Int {
        val digitsBefore = original.take(offset).count { it.isDigit() }
        if (digitsBefore == 0) {
            return transformed.indexOfFirst { it.isDigit() }.takeIf { it >= 0 } ?: 0
        }
        if (digitsBefore >= transformedDigits.size) return transformed.length
        return transformedDigits[digitsBefore - 1] + 1
    }

    override fun transformedToOriginal(offset: Int): Int {
        val digitsBefore = transformed.take(offset).count { it.isDigit() }
        if (digitsBefore == 0) {
            return original.indexOfFirst { it.isDigit() }.takeIf { it >= 0 } ?: 0
        }
        if (digitsBefore >= originalDigits.size) return original.length
        return originalDigits[digitsBefore - 1] + 1
    }

    private fun digitIndexes(value: String): List<Int> =
        value.mapIndexedNotNull { index, char -> index.takeIf { char.isDigit() } }
}
