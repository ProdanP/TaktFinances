package com.prodan.taktfinances.accounts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prodan.taktfinances.CollectEffects
import com.prodan.taktfinances.accounts.domain.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.AccountRepository
import com.prodan.taktfinances.accounts.domain.AccountType
import com.prodan.taktfinances.accounts.domain.AddAccount
import com.prodan.taktfinances.accounts.domain.Bank
import com.prodan.taktfinances.accounts.domain.BankAccountKind
import com.prodan.taktfinances.accounts.domain.Currency
import com.prodan.taktfinances.accounts.domain.FormField
import com.prodan.taktfinances.accounts.domain.InvestmentKind
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme

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
                title = { Text("New account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = { SaveBar(state, onIntent) },
    ) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                AccountPreviewCard(state, onIntent)
                Spacer(Modifier.height(16.dp))
                TypeToggle(state, onIntent)
                SectionLabel("Name", isError = FormField.Name in state.visibleErrors)
                FormTextField(
                    field = FormField.Name,
                    value = state.name,
                    onValueChange = { onIntent(AddAccountIntent.NameChanged(it)) },
                    placeholder = "Salary, Savings, …",
                    leadingIcon = Icons.AutoMirrored.Outlined.Label,
                    state = state,
                    onIntent = onIntent,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
                AddAccountDetails(state = state, onIntent = onIntent)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Connected toggle buttons for the account type, each with an icon. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TypeToggle(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = Modifier.fillMaxWidth(),
    ) {
        state.types.forEachIndexed { index, type ->
            val checked = type == state.type
            // The checked button widens and shows its icon, so labels fit even on 360dp phones.
            val weight by animateFloatAsState(if (checked) 1.3f else 1f)
            ToggleButton(
                checked = checked,
                onCheckedChange = { onIntent(AddAccountIntent.TypeSelected(type)) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    state.types.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                modifier = Modifier.weight(weight),
            ) {
                if (checked) {
                    Icon(type.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                }
                Text(type.title, maxLines = 1, softWrap = false)
            }
        }
    }
}

/** Hint and "Add account" pinned above the navigation bar and the keyboard. */
@Composable
private fun SaveBar(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                val hint = state.hint
                if (hint != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Button(
                    onClick = { onIntent(AddAccountIntent.SaveTapped) },
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { if (hint != null) stateDescription = hint },
                ) {
                    Text("Add account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AddAccountPreview(state: AddAccountState) {
    MaterialExpressiveTheme(colorScheme = financesColorScheme(dark = false, contrast = ThemeContrast.Standard)) {
        AddAccountContent(state = state, onIntent = {}, onBack = {})
    }
}

@Preview
@Composable
private fun AddAccountEmptyPreview() {
    AddAccountPreview(AddAccountState())
}

@Preview
@Composable
private fun AddAccountBankFilledPreview() {
    AddAccountPreview(
        AddAccountState(
            name = "Salary",
            amount = "2500",
            details = AccountDetailsDraft(
                bank = Bank.Victoriabank,
                bankAccountKind = BankAccountKind.Savings,
                last4 = "0421",
            ),
        ),
    )
}

@Preview
@Composable
private fun AddAccountInvestmentFilledPreview() {
    AddAccountPreview(
        AddAccountState(
            type = AccountType.Investment,
            currency = Currency.USD,
            name = "Crypto stash",
            amount = "1250",
            details = AccountDetailsDraft(investmentKind = InvestmentKind.Crypto, platform = "Binance"),
        ),
    )
}
