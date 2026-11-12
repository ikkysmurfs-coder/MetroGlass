/**
 * MetroGlass (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metroglass.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassCard(
    cornerRadius: Dp = 24.dp,
    isDark: Boolean = isSystemInDarkTheme()
): Modifier {
    val backgroundColor = if (isDark) {
        Color(0x14FFFFFF) // White 8% opacity
    } else {
        Color(0x0F000000) // Black 6% opacity
    }
    val borderColor = if (isDark) {
        Color(0x1FFFFFFF) // White 12% opacity
    } else {
        Color(0x1A000000) // Black 10% opacity
    }
    val highlightColor = if (isDark) {
        Color(0x4DFFFFFF) // White 30% top inner highlight
    } else {
        Color(0x33000000) // Black 20% top inner highlight
    }

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
        .drawBehind {
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(highlightColor, Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                ),
                strokeWidth = 1.dp.toPx(),
                start = Offset(0f, 0.5f),
                end = Offset(size.width, 0.5f)
            )
        }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glassCard(cornerRadius = cornerRadius),
        content = content
    )
}
