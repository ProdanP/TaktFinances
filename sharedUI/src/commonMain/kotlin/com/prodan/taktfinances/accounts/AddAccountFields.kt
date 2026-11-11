package com.prodan.taktfinances.accounts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.domain.FormField

private val FieldShape = RoundedCornerShape(16.dp)

/** Error message shown under a field once the user has edited and left it. */
internal fun FormField.errorText(): String = when (this) {
    FormField.Name -> "Enter a name"
    FormField.Amount -> "Enter an amount"
    FormField.Bank -> "Choose a bank"
    FormField.AccountKind -> "Choose an account kind"
    FormField.Last4 -> "Enter exactly 4 digits"
    FormField.InvestmentKind -> "Choose an investment kind"
    FormField.Platform -> "Enter a platform"
    FormField.Direction -> "Choose Borrowed or Lent"
    FormField.Counterparty -> "Enter who the loan is with"
}

@Composable
internal fun SectionLabel(text: String, isError: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
internal fun FieldError(text: String, color: Color = MaterialTheme.colorScheme.error, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(top = 6.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

/** Filled, borderless text field that reports focus changes for [field]. */
@Composable
internal fun FormTextField(
    field: FormField,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null,
) {
    val isError = field in state.visibleErrors
    val colors = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        prefix = prefix?.let { { Text(it, color = colors.onSurfaceVariant) } },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        shape = FieldShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = colors.surfaceContainerHigh,
            unfocusedContainerColor = colors.surfaceContainerHigh,
            errorContainerColor = colors.errorContainer.copy(alpha = 0.18f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isError) Modifier.border(2.dp, colors.error, FieldShape) else Modifier)
            .onFocusChanged { onIntent(AddAccountIntent.FieldFocusChanged(field, it.isFocused)) },
    )
    if (isError) FieldError(field.errorText())
}

/** Single-select chips that wrap onto more lines when needed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceChips(
    options: List<T>,
    selected: T?,
    title: (T) -> String,
    isError: Boolean,
    onSelected: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option) },
                label = { Text(title(option)) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }
    }
}
