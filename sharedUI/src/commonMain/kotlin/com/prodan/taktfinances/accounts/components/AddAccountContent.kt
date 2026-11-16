package com.prodan.taktfinances.accounts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.AdaptiveLayout
import com.prodan.taktfinances.WidthClass
import com.prodan.taktfinances.accounts.AddAccountIntent
import com.prodan.taktfinances.accounts.AddAccountState
import com.prodan.taktfinances.accounts.domain.entity.AccountDetailsDraft
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.BankAccountKind
import com.prodan.taktfinances.accounts.domain.entity.Currency
import com.prodan.taktfinances.accounts.domain.entity.FormField
import com.prodan.taktfinances.accounts.domain.entity.InvestmentKind
import com.prodan.taktfinances.theme.ThemeContrast
import com.prodan.taktfinances.theme.financesColorScheme
import com.prodan.taktfinances.widthClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccountContent(
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
    onBack: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Preview beside the form only when there is room for both and enough height to keep
        // the preview pinned; otherwise one scrolling column (phones, phone landscape).
        val twoColumns = widthClass(maxWidth) == WidthClass.Wide && maxHeight >= AdaptiveLayout.ShortHeight
        val columnWidth = if (widthClass(maxWidth) == WidthClass.Compact) AdaptiveLayout.PhoneColumnWidth else AdaptiveLayout.FormColumnWidth
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
            bottomBar = { if (!twoColumns) SaveBar(state, onIntent, columnWidth) },
        ) { padding ->
            if (twoColumns) {
                TwoColumnForm(state, onIntent, Modifier.fillMaxSize().padding(padding))
            } else {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = columnWidth)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        AccountPreviewCard(state, onIntent)
                        Spacer(Modifier.height(16.dp))
                        FormFields(state, onIntent)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/** Wide windows: the preview stays in view on the left while the form scrolls on the right. */
@Composable
private fun TwoColumnForm(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit, modifier: Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        modifier = modifier.padding(horizontal = 24.dp),
    ) {
        Column(Modifier.width(PREVIEW_COLUMN_WIDTH).padding(vertical = 8.dp)) {
            AccountPreviewCard(state, onIntent)
        }
        Column(Modifier.widthIn(max = AdaptiveLayout.FormColumnWidth).weight(1f, fill = false).fillMaxHeight()) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
            ) {
                FormFields(state, onIntent)
                Spacer(Modifier.height(24.dp))
            }
            SaveBar(state, onIntent, AdaptiveLayout.FormColumnWidth, horizontalPadding = 0.dp)
        }
    }
}

@Composable
private fun FormFields(state: AddAccountState, onIntent: (AddAccountIntent) -> Unit) {
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
    if (!state.showsAmount) InvestmentTrackingNotice()
    AddAccountDetails(state = state, onIntent = onIntent)
}

internal const val INVESTMENT_TRACKING_NOTICE =
    "Investments are tracked by trades. You'll add your holdings as trades after saving."

/** Shown for Investment in place of an amount. */
@Composable
private fun InvestmentTrackingNotice() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(INVESTMENT_TRACKING_NOTICE, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val PREVIEW_COLUMN_WIDTH = 360.dp

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

internal const val SAVE_FAILED_MESSAGE = "Couldn't save the account. Try again."

/** Hint and "Add account" pinned above the navigation bar and the keyboard. */
@Composable
private fun SaveBar(
    state: AddAccountState,
    onIntent: (AddAccountIntent) -> Unit,
    maxWidth: Dp,
    horizontalPadding: Dp = 16.dp,
) {
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
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
            ) {
                val hint = state.hint
                if (state.saveFailed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = SAVE_FAILED_MESSAGE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else if (hint != null) {
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
