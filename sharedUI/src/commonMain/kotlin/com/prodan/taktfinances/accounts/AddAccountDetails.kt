package com.prodan.taktfinances.accounts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.FormField

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
    val errors = state.visibleErrors
    SectionLabel("Bank", isError = FormField.Bank in errors)
    BankRow(state.banks, state.details.bank) { onIntent(AddAccountIntent.BankSelected(it)) }
    if (FormField.Bank in errors) FieldError(FormField.Bank.errorText())

    SectionLabel("Account kind", isError = FormField.AccountKind in errors)
    ChoiceChips(
        options = state.bankAccountKinds,
        selected = state.details.bankAccountKind,
        title = { it.title },
        isError = FormField.AccountKind in errors,
        onSelected = { onIntent(AddAccountIntent.BankAccountKindSelected(it)) },
    )
    if (FormField.AccountKind in errors) FieldError(FormField.AccountKind.errorText())

    SectionLabel("Last 4 digits · optional")
    FormTextField(
        field = FormField.Last4,
        value = state.details.last4,
        onValueChange = { onIntent(AddAccountIntent.Last4Changed(it)) },
        placeholder = "0000",
        leadingIcon = Icons.Outlined.CreditCard,
        prefix = "•••• ",
        state = state,
        onIntent = onIntent,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
    )
}

/** Horizontally scrolling bank badges; the row is cut at the edge on phones to show it scrolls. */
@Composable
private fun BankRow(banks: List<Bank>, selected: Bank?, onSelected: (Bank) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(end = 16.dp),
    ) {
        items(banks) { bank ->
            val isSelected = bank == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelected(bank) })
                    .padding(vertical = 4.dp),
            ) {
                Box(
                    Modifier
                        .border(2.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        .padding(4.dp),
                ) {
                    BankIcon(bank, size = 44.dp)
                }
                Text(
                    text = bank.shortName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun InvestmentDetailsSection(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    val errors = state.visibleErrors
    SectionLabel("Investment kind", isError = FormField.InvestmentKind in errors)
    ChoiceChips(
        options = state.investmentKinds,
        selected = state.details.investmentKind,
        title = { it.title },
        isError = FormField.InvestmentKind in errors,
        onSelected = { onIntent(AddAccountIntent.InvestmentKindSelected(it)) },
    )
    if (FormField.InvestmentKind in errors) FieldError(FormField.InvestmentKind.errorText())

    SectionLabel("Platform")
    FormTextField(
        field = FormField.Platform,
        value = state.details.platform,
        onValueChange = { onIntent(AddAccountIntent.PlatformChanged(it)) },
        placeholder = "Binance, XTB, …",
        leadingIcon = Icons.Outlined.Public,
        state = state,
        onIntent = onIntent,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}

@Composable
private fun LoanDetailsSection(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    val errors = state.visibleErrors
    SectionLabel("Direction", isError = FormField.Direction in errors)
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
    if (FormField.Direction in errors) FieldError(FormField.Direction.errorText())

    SectionLabel("Counterparty")
    FormTextField(
        field = FormField.Counterparty,
        value = state.details.counterparty,
        onValueChange = { onIntent(AddAccountIntent.CounterpartyChanged(it)) },
        placeholder = "Who the loan is with",
        leadingIcon = Icons.Outlined.Person,
        state = state,
        onIntent = onIntent,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}
