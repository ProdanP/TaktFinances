package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.AdaptiveLayout
import com.prodan.taktfinances.WidthClass
import com.prodan.taktfinances.accounts.AccountDetailContent
import com.prodan.taktfinances.accounts.AccountDetailIntent
import com.prodan.taktfinances.accounts.AccountDetailState
import com.prodan.taktfinances.accounts.models.AccountDetailHeaderModel
import com.prodan.taktfinances.accounts.models.ChangeModel
import com.prodan.taktfinances.accounts.models.TimelineRowModel
import com.prodan.taktfinances.theme.ChangeDirection
import com.prodan.taktfinances.theme.changeColor
import com.prodan.taktfinances.widthClass

internal const val DETAIL_LOAD_FAILED_MESSAGE = "Couldn't load this account"

/** Account Detail: one scrolling column, or header and chart beside the timeline when two panes fit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountDetailContent(state: AccountDetailState, onIntent: (AccountDetailIntent) -> Unit, onBack: () -> Unit) {
    val loaded = state.content as? AccountDetailContent.Loaded
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(loaded?.header?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val wide = widthClass(maxWidth) == WidthClass.Wide
            when (val content = state.content) {
                AccountDetailContent.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                AccountDetailContent.Failed -> LoadFailed(onRetry = { onIntent(AccountDetailIntent.RetryTapped) })
                is AccountDetailContent.Loaded -> {
                    if (wide) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        ) {
                            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                                Header(content.header)
                                Spacer(Modifier.height(24.dp))
                                Chart(state, content, onIntent, chartHeight = 280.dp)
                            }
                            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                                Activity(content, onIntent, wide = true)
                            }
                        }
                    } else {
                        Column(
                            Modifier
                                .widthIn(max = AdaptiveLayout.PhoneColumnWidth)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Header(content.header)
                            Spacer(Modifier.height(20.dp))
                            Chart(state, content, onIntent, chartHeight = 170.dp)
                            Spacer(Modifier.height(16.dp))
                            Activity(content, onIntent, wide = false)
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                    state.sheet?.let { CheckInSheetHost(it, content.header.currency, wide, onIntent) }
                }
            }
        }
    }
}

@Composable
private fun Header(header: AccountDetailHeaderModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val bank = header.bank
        if (bank != null) {
            BankIcon(bank, size = 40.dp)
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            ) {
                Icon(header.type.icon, contentDescription = header.type.title, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Column {
            Text(header.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!header.last4.isNullOrEmpty()) {
                Text("•••• ${header.last4}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .padding(top = 12.dp)
            .clearAndSetSemantics { contentDescription = "Balance ${header.balance} ${header.currency}" },
    ) {
        Text(header.balance, style = MaterialTheme.typography.displayMedium)
        Text(
            header.currency,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        )
    }
    val change = header.thisMonth
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .padding(top = 4.dp)
            .clearAndSetSemantics { contentDescription = "This month: ${change?.spoken ?: "no previous month"}" },
    ) {
        Text("This month", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (change == null) {
            Text("—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            ChangeText(change, withIcon = true)
        }
    }
}

@Composable
private fun ChangeText(change: ChangeModel, withIcon: Boolean, small: Boolean = false) {
    val color = changeColor(change.direction)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (withIcon) {
            val icon = when (change.direction) {
                ChangeDirection.Up -> Icons.AutoMirrored.Filled.TrendingUp
                ChangeDirection.Down -> Icons.AutoMirrored.Filled.TrendingDown
                ChangeDirection.Flat -> Icons.AutoMirrored.Filled.TrendingFlat
            }
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(
            change.text,
            style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
            color = color,
        )
    }
}

@Composable
private fun Chart(state: AccountDetailState, content: AccountDetailContent.Loaded, onIntent: (AccountDetailIntent) -> Unit, chartHeight: Dp) {
    BalanceChart(
        points = state.chart,
        currency = content.header.currency,
        range = state.range,
        onRangeSelected = { onIntent(AccountDetailIntent.RangeSelected(it)) },
        chartHeight = chartHeight,
    )
}

/** "Check in" and the monthly timeline, or "No trades yet" for an account tracked by trades. */
@Composable
private fun Activity(content: AccountDetailContent.Loaded, onIntent: (AccountDetailIntent) -> Unit, wide: Boolean) {
    val timeline = content.timeline
    if (timeline == null) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No trades yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    if (!wide) {
        CheckInButton(onIntent, Modifier.fillMaxWidth().height(56.dp))
        Spacer(Modifier.height(20.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
        if (wide) CheckInButton(onIntent)
    }
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            timeline.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                TimelineRow(row, onIntent)
            }
        }
    }
}

@Composable
private fun CheckInButton(onIntent: (AccountDetailIntent) -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = { onIntent(AccountDetailIntent.CheckInTapped) }, modifier = modifier) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text("Check in")
    }
}

@Composable
private fun TimelineRow(row: TimelineRowModel, onIntent: (AccountDetailIntent) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Edit ${row.label}") { onIntent(AccountDetailIntent.MonthTapped(row.month)) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                row.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (row.checkedIn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!row.checkedIn) {
                Text(
                    "Not checked in",
                    style = MaterialTheme.typography.labelMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.semantics(mergeDescendants = true) {
                row.change?.let { contentDescription = "${row.balance}, ${it.spoken}" }
            },
        ) {
            Text(
                row.balance,
                style = MaterialTheme.typography.bodyLarge,
                color = if (row.checkedIn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                !row.checkedIn -> Unit
                row.change != null -> ChangeText(row.change, withIcon = false, small = true)
                else -> Text("Start", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!row.checkedIn) {
            TextButton(onClick = { onIntent(AccountDetailIntent.MonthTapped(row.month)) }) { Text("Check in") }
        }
    }
}

@Composable
private fun LoadFailed(onRetry: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = AdaptiveLayout.PhoneColumnWidth).fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)) {
                Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(DETAIL_LOAD_FAILED_MESSAGE, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Try again") }
            }
        }
    }
}
