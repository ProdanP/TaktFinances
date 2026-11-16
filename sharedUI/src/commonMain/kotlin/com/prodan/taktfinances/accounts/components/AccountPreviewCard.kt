package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.AddAccountIntent
import com.prodan.taktfinances.accounts.AddAccountState
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.FormField
import com.prodan.taktfinances.accounts.domain.entity.accountSummary
import com.prodan.taktfinances.accounts.domain.entity.formatAmount

internal val AccountType.icon: ImageVector
    get() = when (this) {
        AccountType.BankAccount -> Icons.Filled.AccountBalance
        AccountType.Investment -> Icons.AutoMirrored.Filled.TrendingUp
        AccountType.Loan -> Icons.Filled.SwapHoriz
    }

/**
 * Live preview of the account being created; the amount and currency are edited in place. An
 * Investment has no amount: the preview shows 0.00 and only the currency can be changed.
 */
@Composable
internal fun AccountPreviewCard(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    AccountCard(
        type = state.type,
        bank = state.details.bank,
        name = state.name,
        last4 = state.details.last4,
        amountLabel = if (state.showsAmount) AMOUNT_LABEL else "Balance",
        footer = {
            if (state.showsAmount && FormField.Amount in state.visibleErrors) {
                FieldError(FormField.Amount.errorText(), color = MaterialTheme.colorScheme.errorContainer)
            } else {
                AccountCardSummary(accountSummary(state.type, state.details).orEmpty())
            }
        },
    ) {
        if (state.showsAmount) {
            AmountInput(state, onIntent, Modifier.weight(1f))
        } else {
            val onPrimary = MaterialTheme.colorScheme.onPrimary
            Text(
                text = formatAmount(0),
                style = MaterialTheme.typography.displaySmall.copy(color = onPrimary.copy(alpha = 0.6f), fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
            )
        }
        CurrencyPill(state, onIntent)
    }
}

/** Label of the Bank and Loan amount: the balance becomes the current month's entry. */
private const val AMOUNT_LABEL = "Balance this month"

@Composable
private fun AmountInput(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit, modifier: Modifier) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val style = MaterialTheme.typography.displaySmall.copy(color = onPrimary, fontWeight = FontWeight.Medium)
    BasicTextField(
        value = state.amountText,
        onValueChange = { onIntent(AddAccountIntent.AmountChanged(it)) },
        textStyle = style,
        singleLine = true,
        cursorBrush = SolidColor(onPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        decorationBox = { inner ->
            Box {
                if (state.amountText.isEmpty()) Text("0.00", style = style.copy(color = onPrimary.copy(alpha = 0.45f)))
                inner()
            }
        },
        modifier = modifier
            .semantics { contentDescription = AMOUNT_LABEL }
            .onFocusChanged { onIntent(AddAccountIntent.FieldFocusChanged(FormField.Amount, it.isFocused)) },
    )
}

@Composable
private fun CurrencyPill(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    // Menu expansion is transient widget state, like the dropdowns this screen used before.
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Text(state.currency.code, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change currency", modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.code) },
                    onClick = {
                        onIntent(AddAccountIntent.CurrencySelected(currency))
                        expanded = false
                    },
                )
            }
        }
    }
}
