package com.imladris.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.theme.GlassBackground
import com.imladris.core.ui.theme.GlassBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassBackground,
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun EtherealGlow(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF64FFDA),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .blur(48.dp)
                .background(color.copy(alpha = 0.25f), RoundedCornerShape(100))
        )
        content()
    }
}

@Composable
fun ImladrisDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFD4AF37).copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}
