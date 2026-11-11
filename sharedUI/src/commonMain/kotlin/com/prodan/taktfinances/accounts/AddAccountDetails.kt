package com.prodan.taktfinances.accounts

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Bank

@Composable
internal fun AddAccountDetails(
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
) {
    when (state.type) {
        AccountType.BankAccount -> BankDetailsSection(state, onIntent)
        AccountType.Investment -> InvestmentDetailsSection(state, onIntent)
        AccountType.Loan -> LoanDetailsSection(state, onIntent)
    }
}

@Composable
private fun BankDetailsSection(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    val details = state.details
    BankField(state, onIntent)
    Spacer(Modifier.height(8.dp))
    ChoiceField(
        label = "Account kind",
        options = state.bankAccountKinds,
        selected = details.bankAccountKind,
        optionTitle = { it.title },
        isError = state.detailErrors.accountKind,
        errorText = "Choose an account kind",
        onSelected = { onIntent(AddAccountIntent.BankAccountKindSelected(it)) },
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = details.last4,
        onValueChange = { onIntent(AddAccountIntent.Last4Changed(it)) },
        label = { Text("Last 4 digits of account or card (optional)") },
        singleLine = true,
        isError = state.detailErrors.last4,
        supportingText = if (state.detailErrors.last4) {
            { Text("Enter exactly 4 digits") }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InvestmentDetailsSection(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    ChoiceField(
        label = "Investment kind",
        options = state.investmentKinds,
        selected = state.details.investmentKind,
        optionTitle = { it.title },
        isError = state.detailErrors.investmentKind,
        errorText = "Choose an investment kind",
        onSelected = { onIntent(AddAccountIntent.InvestmentKindSelected(it)) },
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.details.platform,
        onValueChange = { onIntent(AddAccountIntent.PlatformChanged(it)) },
        label = { Text("Platform") },
        singleLine = true,
        isError = state.detailErrors.platform,
        supportingText = if (state.detailErrors.platform) {
            { Text("Enter a platform") }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LoanDetailsSection(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        state.loanDirections.forEachIndexed { index, direction ->
            SegmentedButton(
                selected = direction == state.details.loanDirection,
                onClick = { onIntent(AddAccountIntent.LoanDirectionSelected(direction)) },
                shape = SegmentedButtonDefaults.itemShape(index, state.loanDirections.size),
                label = { Text(direction.title, maxLines = 1) },
            )
        }
    }
    if (state.detailErrors.direction) {
        Text(
            text = "Choose Borrowed or Lent",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        )
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.details.counterparty,
        onValueChange = { onIntent(AddAccountIntent.CounterpartyChanged(it)) },
        label = { Text("Counterparty") },
        singleLine = true,
        isError = state.detailErrors.counterparty,
        supportingText = if (state.detailErrors.counterparty) {
            { Text("Enter who the loan is with") }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankField(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected: Bank? = state.details.bank
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.title.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Bank") },
            leadingIcon = selected?.let { { BankIcon(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = state.detailErrors.bank,
            supportingText = if (state.detailErrors.bank) {
                { Text("Choose a bank") }
            } else {
                null
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.banks.forEach { bank ->
                DropdownMenuItem(
                    text = { Text(bank.title) },
                    leadingIcon = { BankIcon(bank) },
                    onClick = {
                        onIntent(AddAccountIntent.BankSelected(bank))
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceField(
    label: String,
    options: List<T>,
    selected: T?,
    optionTitle: (T) -> String,
    isError: Boolean,
    errorText: String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let(optionTitle).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = if (isError) {
                { Text(errorText) }
            } else {
                null
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionTitle(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
