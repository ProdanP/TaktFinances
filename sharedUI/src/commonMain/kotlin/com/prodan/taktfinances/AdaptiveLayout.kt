package com.prodan.taktfinances

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout breakpoints, measured on the space a screen's content actually gets (after the
 * navigation bar, rail or drawer), not on the window. See docs/system/adaptive-ui.md.
 */
internal object AdaptiveLayout {
    /** Below this width the content is phone-sized. */
    val MediumWidth: Dp = 600.dp

    /** From this width two panes fit side by side. */
    val TwoPaneWidth: Dp = 840.dp

    /** Below this height a pane cannot stay pinned without hiding the content beside it. */
    val ShortHeight: Dp = 480.dp

    /** Readable width of a single phone-like column on larger screens. */
    val PhoneColumnWidth: Dp = 480.dp

    /** Readable width of a form column. */
    val FormColumnWidth: Dp = 560.dp
}

internal enum class WidthClass { Compact, Medium, Wide }

internal fun widthClass(width: Dp): WidthClass = when {
    width >= AdaptiveLayout.TwoPaneWidth -> WidthClass.Wide
    width >= AdaptiveLayout.MediumWidth -> WidthClass.Medium
    else -> WidthClass.Compact
}
