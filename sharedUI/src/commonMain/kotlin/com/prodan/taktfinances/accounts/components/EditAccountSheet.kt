package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.prodan.taktfinances.accounts.AccountDetailIntent
import com.prodan.taktfinances.accounts.models.EDIT_SAVE_FAILED_MESSAGE
import com.prodan.taktfinances.accounts.models.EditSheetModel

/** The "Edit account" form: a bottom sheet on compact widths, a dialog on wide windows, like the check-in sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditAccountSheetHost(edit: EditSheetModel, currency: String, wide: Boolean, onIntent: (AccountDetailIntent) -> Unit) {
    val dismiss = { onIntent(AccountDetailIntent.EditDismissed) }
    if (wide) {
        Dialog(onDismissRequest = dismiss) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.widthIn(max = 480.dp),
            ) {
                EditAccountForm(edit, currency, onIntent, Modifier.padding(24.dp))
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            EditAccountForm(edit, currency, onIntent, Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun EditAccountForm(edit: EditSheetModel, currency: String, onIntent: (AccountDetailIntent) -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text("Edit account", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = edit.name,
            onValueChange = { onIntent(AccountDetailIntent.EditNameChanged(it)) },
            label = { Text("Name") },
            isError = edit.name.isBlank(),
            supportingText = if (edit.name.isBlank()) ({ Text("Enter a name") }) else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        if (edit.showsAmount) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = edit.amountText,
                onValueChange = { onIntent(AccountDetailIntent.EditAmountChanged(it)) },
                label = { Text("Balance this month") },
                placeholder = { Text("0.00") },
                suffix = { Text(currency) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onIntent(AccountDetailIntent.EditAmountFocusChanged(it.isFocused)) },
            )
        }
        if (edit.failed) {
            Text(
                EDIT_SAVE_FAILED_MESSAGE,
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
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onIntent(AccountDetailIntent.EditDismissed) }, enabled = !edit.saving) { Text("Cancel") }
            Button(onClick = { onIntent(AccountDetailIntent.EditSaveTapped) }, enabled = edit.canSave) { Text("Save") }
        }
    }
}
