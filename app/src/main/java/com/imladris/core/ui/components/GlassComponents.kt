package com.imladris.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.theme.CelestialBlue
import com.imladris.core.ui.theme.DeepMist
import com.imladris.core.ui.theme.GlassBackground
import com.imladris.core.ui.theme.GlassBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    containerColor: Color = Color(0xFF121721).copy(alpha = 0.82f),
    borderColor: Color = Color(0xFF79C0FF).copy(alpha = 0.18f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = shape
            )
            .padding(contentPadding)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun EtherealGlow(
    modifier: Modifier = Modifier,
    color: Color = CelestialBlue,
    sizeDp: Dp = 100.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .blur(36.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(100))
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
                        CelestialBlue.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}
