package com.prodan.taktfinances.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.accountSummary

internal val AccountType.icon: ImageVector
    get() = when (this) {
        AccountType.BankAccount -> Icons.Filled.AccountBalance
        AccountType.Investment -> Icons.AutoMirrored.Filled.TrendingUp
        AccountType.Loan -> Icons.Filled.SwapHoriz
    }

/** Live preview of the account being created; the amount and currency are edited in place. */
@Composable
internal fun AccountPreviewCard(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.primary,
        contentColor = colors.onPrimary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PreviewBadge(state)
                Spacer(Modifier.size(10.dp))
                Text(
                    text = state.name.ifBlank { "Account name" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.name.isBlank()) colors.onPrimary.copy(alpha = 0.6f) else colors.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                val last4 = state.details.last4
                if (state.type == AccountType.BankAccount && last4.isNotEmpty()) {
                    Text(
                        text = "•••• $last4",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 1.sp,
                        color = colors.onPrimary.copy(alpha = 0.85f),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Starting balance",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onPrimary.copy(alpha = 0.8f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AmountInput(state, onIntent, Modifier.weight(1f))
                CurrencyPill(state, onIntent)
            }
            if (FormField.Amount in state.visibleErrors) {
                FieldError(FormField.Amount.errorText(), color = colors.errorContainer)
            } else {
                Text(
                    text = accountSummary(state.type, state.details).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimary.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp).height(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PreviewBadge(state: AddAccountState) {
    val bank = state.details.bank
    if (state.type == AccountType.BankAccount && bank != null) {
        BankIcon(bank, size = 32.dp)
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
        ) {
            Icon(state.type.icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

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
            .semantics { contentDescription = "Starting balance" }
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
