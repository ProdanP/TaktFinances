package com.prodan.taktfinances.accounts.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prodan.taktfinances.accounts.models.ChartPointModel
import com.prodan.taktfinances.accounts.models.ChartRange
import com.prodan.taktfinances.accounts.models.summary

/**
 * Month-end balances as a line with a range choice. Tapping a point, or hovering it with a pointer,
 * labels it with its month and balance. Screen readers get a text summary instead of the drawing.
 */
@Composable
internal fun BalanceChart(
    points: List<ChartPointModel>,
    currency: String,
    range: ChartRange,
    onRangeSelected: (ChartRange) -> Unit,
    chartHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("Balance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(top = 8.dp))
                SingleChoiceSegmentedButtonRow(Modifier.width(210.dp)) {
                    ChartRange.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = option == range,
                            onClick = { onRangeSelected(option) },
                            shape = SegmentedButtonDefaults.itemShape(index, ChartRange.entries.size),
                        ) { Text(option.title) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (points.isNotEmpty()) {
                LineChart(points, currency, Modifier.fillMaxWidth().height(chartHeight))
            }
            if (points.size == 1) {
                Text(
                    ONE_MONTH_NOTE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

internal const val ONE_MONTH_NOTE = "The chart fills in as months pass"

@Composable
private fun LineChart(points: List<ChartPointModel>, currency: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val axisStyle = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant)
    val tipStyle = TextStyle(color = colors.inverseOnSurface, fontSize = axisStyle.fontSize, fontWeight = FontWeight.Medium)
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    val currentPoints by rememberUpdatedState(points)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = points.summary(currency) }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val x = event.changes.firstOrNull()?.position?.x ?: continue
                        when (event.type) {
                            PointerEventType.Exit -> selected = null
                            PointerEventType.Move, PointerEventType.Press, PointerEventType.Enter ->
                                selected = nearestIndex(x, size.width.toFloat(), currentPoints.size)
                        }
                    }
                }
            },
    ) {
        val leftPad = 8.dp.toPx()
        val rightPad = 8.dp.toPx()
        val axisHeight = 20.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = size.height - axisHeight
        val values = points.map { it.balanceMinor }
        val max = values.max()
        val min = values.min()
        val span = (max - min).takeIf { it > 0 } ?: maxOf(max / 10, 100)
        val lo = min - span * 0.1
        val hi = max + span * 0.1
        fun xAt(i: Int) = if (points.size == 1) size.width / 2 else leftPad + i * (size.width - leftPad - rightPad) / (points.size - 1)
        fun yAt(v: Long) = (bottom - (v - lo) / (hi - lo) * (bottom - top)).toFloat()

        repeat(4) { g ->
            val y = top + g * (bottom - top) / 3
            drawLine(colors.surfaceContainerHighest, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val offsets = points.indices.map { Offset(xAt(it), yAt(values[it])) }
        if (offsets.size > 1) {
            val line = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(offsets.last().x, bottom)
                lineTo(offsets.first().x, bottom)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.22f), colors.primary.copy(alpha = 0f)), top, bottom))
            drawPath(line, colors.primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        selected?.let { i ->
            drawLine(
                colors.outline, Offset(offsets[i].x, top), Offset(offsets[i].x, bottom),
                strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }
        offsets.forEachIndexed { i, o ->
            val radius = if (i == selected) 6.dp.toPx() else 4.dp.toPx()
            drawCircle(colors.surfaceContainerLow, radius + 2.dp.toPx(), o)
            drawCircle(colors.primary, radius, o)
            // Label every month that fits: all of them up to 12, then every n-th.
            val step = (points.size + 11) / 12
            if (i % step == 0 || i == points.lastIndex) {
                val label = measurer.measure(points[i].axisLabel, axisStyle)
                val lx = (o.x - label.size.width / 2).coerceIn(0f, size.width - label.size.width)
                drawText(label, topLeft = Offset(lx, size.height - label.size.height))
            }
        }
        selected?.let { i ->
            val tip = measurer.measure("${points[i].label}\n${points[i].balance} $currency", tipStyle)
            val pad = 8.dp.toPx()
            val w = tip.size.width + pad * 2
            val h = tip.size.height + pad * 2
            val x = (offsets[i].x - w / 2).coerceIn(0f, size.width - w)
            val y = (offsets[i].y - h - 10.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(colors.inverseSurface, Offset(x, y), Size(w, h), CornerRadius(8.dp.toPx()))
            drawText(tip, topLeft = Offset(x + pad, y + pad))
        }
    }
}

private fun nearestIndex(x: Float, width: Float, count: Int): Int? {
    if (count == 0) return null
    if (count == 1) return 0
    return ((x / width) * (count - 1)).let { kotlin.math.round(it).toInt() }.coerceIn(0, count - 1)
}
