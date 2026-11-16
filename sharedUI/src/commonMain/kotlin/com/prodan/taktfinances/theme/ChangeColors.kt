package com.prodan.taktfinances.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Direction of a balance change; the sign and an icon always accompany the color. */
enum class ChangeDirection { Up, Down, Flat }

/** Success green for an increase, the scheme's error color for a decrease, neutral for no change. */
@Composable
fun changeColor(direction: ChangeDirection): Color = when (direction) {
    ChangeDirection.Up -> if (isSystemInDarkTheme()) Color(0xFF7DD99B) else Color(0xFF1E7A46)
    ChangeDirection.Down -> MaterialTheme.colorScheme.error
    ChangeDirection.Flat -> MaterialTheme.colorScheme.onSurfaceVariant
}
