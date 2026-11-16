package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prodan.taktfinances.accounts.domain.entity.AccountDetails
import com.prodan.taktfinances.accounts.domain.entity.AccountType
import com.prodan.taktfinances.accounts.domain.entity.Bank
import com.prodan.taktfinances.accounts.domain.entity.FinanceAccount
import com.prodan.taktfinances.accounts.domain.entity.accountSummary
import com.prodan.taktfinances.accounts.domain.entity.formatAmount

/**
 * The account card layout shared by the Add Account preview and the My Finances swiper:
 * badge, name, optional last 4 digits, an amount row and a footer line. The amount row is a
 * slot, so the preview can put its editable amount and currency picker there.
 */
@Composable
internal fun AccountCard(
    type: AccountType,
    bank: Bank?,
    name: String,
    last4: String?,
    amountLabel: String,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit,
    amount: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.primary,
        contentColor = colors.onPrimary,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccountBadge(type, bank)
                Spacer(Modifier.size(10.dp))
                Text(
                    text = name.ifBlank { "Account name" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (name.isBlank()) colors.onPrimary.copy(alpha = 0.6f) else colors.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (type == AccountType.BankAccount && !last4.isNullOrEmpty()) {
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
                amountLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onPrimary.copy(alpha = 0.8f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, content = amount)
            footer()
        }
    }
}

/** The one-line details summary under the amount ("Savings · MAIB"). */
@Composable
internal fun AccountCardSummary(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp).height(18.dp),
    )
}

/** A saved account as a read-only card showing its current [balanceMinor]. */
@Composable
internal fun SavedAccountCard(account: FinanceAccount, balanceMinor: Long, modifier: Modifier = Modifier) {
    val details = account.details
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    AccountCard(
        type = account.type,
        bank = (details as? AccountDetails.BankDetails)?.bank,
        name = account.name,
        last4 = (details as? AccountDetails.BankDetails)?.last4,
        amountLabel = "Balance",
        modifier = modifier,
        footer = { AccountCardSummary(accountSummary(details)) },
    ) {
        Text(
            text = formatAmount(balanceMinor),
            style = MaterialTheme.typography.displaySmall.copy(color = onPrimary, fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = account.currency.code,
            style = MaterialTheme.typography.titleMedium,
            color = onPrimary.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun AccountBadge(type: AccountType, bank: Bank?) {
    if (type == AccountType.BankAccount && bank != null) {
        BankIcon(bank, size = 32.dp)
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
        ) {
            Icon(type.icon, contentDescription = type.title, modifier = Modifier.size(18.dp))
        }
    }
}
