package com.prodan.taktfinances.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.AdaptiveLayout
import com.prodan.taktfinances.WidthClass
import com.prodan.taktfinances.accounts.components.SavedAccountCard
import com.prodan.taktfinances.accounts.domain.entity.SavedAccount
import com.prodan.taktfinances.home.HomeIntent
import com.prodan.taktfinances.home.models.MyFinancesContent
import com.prodan.taktfinances.widthClass
import kotlinx.coroutines.flow.distinctUntilChanged

internal const val LOAD_FAILED_MESSAGE = "Couldn't load your accounts"

/** Width of the swiper pane next to "Recent activity" on wide windows. */
private val SwiperPaneWidth = 420.dp

/** The My Finances tab body; picks its layout from the space it gets. */
@Composable
internal fun MyFinancesBody(
    content: MyFinancesContent,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pointerSeen by remember { mutableStateOf(false) }
    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.pointerInput(Unit) {
            // A mouse or trackpad can't swipe the pager, so its first event turns on the arrows.
            awaitPointerEventScope {
                while (!pointerSeen) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes.any { it.type == PointerType.Mouse }) pointerSeen = true
                }
            }
        },
    ) {
        val width = widthClass(maxWidth)
        when (content) {
            MyFinancesContent.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            MyFinancesContent.Failed -> CenteredCard { LoadFailedCard(onRetry = { onIntent(HomeIntent.RetryTapped) }) }
            MyFinancesContent.Empty -> CenteredCard { AddFirstAccountCard(onClick = { onIntent(HomeIntent.AddFirstAccountTapped) }) }
            is MyFinancesContent.Accounts -> {
                val showArrows = pointerSeen || width == WidthClass.Wide
                if (width == WidthClass.Wide) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Column(Modifier.width(SwiperPaneWidth)) {
                            SwiperWithDots(content, showArrows, onIntent)
                        }
                        RecentActivity(
                            account = content.accounts[content.selectedIndex],
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .widthIn(max = AdaptiveLayout.PhoneColumnWidth)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                    ) {
                        SwiperWithDots(content, showArrows, onIntent)
                        Spacer(Modifier.height(16.dp))
                        RecentActivity(
                            account = content.accounts[content.selectedIndex],
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwiperWithDots(content: MyFinancesContent.Accounts, showArrows: Boolean, onIntent: (HomeIntent) -> Unit) {
    AccountSwiper(content, showArrows, onIntent)
    if (content.accounts.size > 1) {
        PageDots(count = content.accounts.size, selected = content.selectedIndex, modifier = Modifier.padding(top = 12.dp))
    }
}

/** Snapping pager of account cards with neighbours peeking; previous/next buttons for pointer users. */
@Composable
private fun AccountSwiper(content: MyFinancesContent.Accounts, showArrows: Boolean, onIntent: (HomeIntent) -> Unit) {
    val pagerState = rememberPagerState(initialPage = content.selectedIndex) { content.accounts.size }
    val currentContent by rememberUpdatedState(content)
    val currentOnIntent by rememberUpdatedState(onIntent)
    val focusRequester = remember { FocusRequester() }

    // The pager reports a settled page; state is the source of truth for the selection.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect { page ->
            val account = currentContent.accounts.getOrNull(page) ?: return@collect
            if (account.id != currentContent.selectedId) currentOnIntent(HomeIntent.AccountSelected(account.id))
        }
    }
    LaunchedEffect(content.selectedIndex, content.accounts.size) {
        if (pagerState.currentPage != content.selectedIndex) pagerState.animateScrollToPage(content.selectedIndex)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> { onIntent(HomeIntent.PreviousTapped); true }
                    Key.DirectionRight -> { onIntent(HomeIntent.NextTapped); true }
                    else -> false
                }
            }
            .focusable(),
    ) {
        if (showArrows) {
            FilledTonalIconButton(
                onClick = { focusRequester.requestFocus(); onIntent(HomeIntent.PreviousTapped) },
                enabled = content.canSelectPrevious,
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous account")
            }
        }
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = if (showArrows) 8.dp else 32.dp),
            pageSpacing = 12.dp,
            key = { content.accounts[it].id },
            modifier = Modifier.weight(1f),
        ) { page ->
            SavedAccountCard(content.accounts[page].account)
        }
        if (showArrows) {
            FilledTonalIconButton(
                onClick = { focusRequester.requestFocus(); onIntent(HomeIntent.NextTapped) },
                enabled = content.canSelectNext,
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next account")
            }
        }
    }
}

@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Account ${selected + 1} of $count" },
    ) {
        repeat(count) { index ->
            val active = index == selected
            Box(
                Modifier
                    .size(width = if (active) 20.dp else 8.dp, height = 8.dp)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape,
                    ),
            )
        }
    }
}

/** Activity of the selected account; empty until transactions exist. */
@Composable
private fun RecentActivity(account: SavedAccount, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Recent activity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                account.account.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Empty and failure states: one card, centred, at a phone-like width on every window. */
@Composable
private fun CenteredCard(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Box(Modifier.widthIn(max = AdaptiveLayout.PhoneColumnWidth).fillMaxWidth()) { content() }
    }
}

@Composable
private fun AddFirstAccountCard(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text("Add your first account", style = MaterialTheme.typography.titleMedium)
            Text(
                "Bank accounts, investments and loans",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoadFailedCard(onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            Text(LOAD_FAILED_MESSAGE, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Try again") }
        }
    }
}
