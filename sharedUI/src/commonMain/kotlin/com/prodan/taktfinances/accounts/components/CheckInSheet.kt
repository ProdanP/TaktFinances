package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.prodan.taktfinances.accounts.AccountDetailIntent
import com.prodan.taktfinances.accounts.CheckInSheet

/** The check-in form: a bottom sheet on compact widths, a dialog on wide windows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckInSheetHost(sheet: CheckInSheet, currency: String, wide: Boolean, onIntent: (AccountDetailIntent) -> Unit) {
    val dismiss = { onIntent(AccountDetailIntent.SheetDismissed) }
    if (wide) {
        Dialog(onDismissRequest = dismiss) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.widthIn(max = 480.dp),
            ) {
                CheckInForm(sheet, currency, onIntent, Modifier.padding(24.dp))
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            CheckInForm(sheet, currency, onIntent, Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding())
        }
    }
    if (sheet.confirmDelete) {
        AlertDialog(
            onDismissRequest = { onIntent(AccountDetailIntent.DeleteCancelled) },
            title = { Text("Delete the check-in for ${sheet.monthLabel}?") },
            confirmButton = {
                TextButton(onClick = { onIntent(AccountDetailIntent.DeleteConfirmed) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onIntent(AccountDetailIntent.DeleteCancelled) }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun CheckInForm(sheet: CheckInSheet, currency: String, onIntent: (AccountDetailIntent) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text("Check in", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Enter this account's balance at the end of the month.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        MonthField(sheet, onIntent)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = sheet.amountText,
            onValueChange = { onIntent(AccountDetailIntent.AmountChanged(it)) },
            label = { Text("Balance") },
            placeholder = { Text("0.00") },
            suffix = { Text(currency) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onIntent(AccountDetailIntent.AmountFocusChanged(it.isFocused)) },
        )
        if (sheet.previousHint != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(
                    sheet.previousHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                AssistChip(
                    onClick = { onIntent(AccountDetailIntent.SameAsLastMonthTapped) },
                    label = { Text("Same as last month") },
                    leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
        sheet.error?.let {
            Text(
                it.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            if (sheet.canDelete) {
                TextButton(onClick = { onIntent(AccountDetailIntent.DeleteTapped) }, enabled = !sheet.saving) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onIntent(AccountDetailIntent.SheetDismissed) }, enabled = !sheet.saving) { Text("Cancel") }
            Button(onClick = { onIntent(AccountDetailIntent.SaveTapped) }, enabled = sheet.canSave) { Text("Save") }
        }
    }
}

/** Months from the current one back to the start month; future months are never offered. */
@Composable
private fun MonthField(sheet: CheckInSheet, onIntent: (AccountDetailIntent) -> Unit) {
    // Menu expansion is transient widget state.
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = sheet.monthLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        // The read-only field swallows taps, so a transparent layer on top opens the menu.
        Surface(
            onClick = { expanded = true },
            color = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.matchParentSize().semantics {
                contentDescription = "Month, ${sheet.monthLabel}"
                role = Role.DropdownList
            },
        ) {}
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sheet.months.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onIntent(AccountDetailIntent.SheetMonthSelected(option.month))
                    },
                )
            }
        }
    }
}
