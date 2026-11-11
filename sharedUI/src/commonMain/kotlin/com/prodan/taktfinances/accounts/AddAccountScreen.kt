package com.prodan.taktfinances.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.AddAccount

@Composable
fun AddAccountScreen(
    repository: AccountRepository,
    onClose: () -> Unit,
    viewModel: AddAccountViewModel = viewModel { AddAccountViewModel(AddAccount(repository)) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            AddAccountEffect.Saved -> onClose()
        }
    }
    AddAccountContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccountContent(
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    state.types.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = type == state.type,
                            onClick = { onIntent(AddAccountIntent.TypeSelected(type)) },
                            shape = SegmentedButtonDefaults.itemShape(index, state.types.size),
                            label = { Text(type.title, maxLines = 1) },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onIntent(AddAccountIntent.NameChanged(it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameInvalid,
                    supportingText = if (state.nameInvalid) {
                        { Text("Enter a name") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                CurrencyField(state = state, onIntent = onIntent)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { onIntent(AddAccountIntent.AmountChanged(it)) },
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = state.amountInvalid,
                    supportingText = if (state.amountInvalid) {
                        {
                            Text(
                                if (state.currency.minorUnitDigits == 0) {
                                    "Enter a whole number"
                                } else {
                                    "Enter a number, up to ${state.currency.minorUnitDigits} decimals"
                                },
                            )
                        }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                AddAccountDetails(state = state, onIntent = onIntent)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onIntent(AddAccountIntent.SaveTapped) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyField(
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = state.currency.code,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
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
