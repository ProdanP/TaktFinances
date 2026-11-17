package com.prodan.taktfinances.accounts.models

/** How many months the chart shows; [months] null means all. */
internal enum class ChartRange(val title: String, val months: Int?) {
    SixMonths("6M", 6),
    OneYear("1Y", 12),
    All("All", null),
}
