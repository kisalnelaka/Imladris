package com.imladris.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.theme.CelestialBlue
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SoftBlack
import kotlin.random.Random

@Composable
fun MajesticBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBlue, SoftBlack)
                )
            )
    ) {
        StarlightCanvas()
        content()
    }
}

@Composable
fun StarlightCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "starlight")
    val stars = remember {
        List(30) {
            StarData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1f,
                alphaSpeed = Random.nextFloat() * 0.02f + 0.01f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val alpha = (Math.sin(System.currentTimeMillis() * star.alphaSpeed.toDouble()).toFloat() + 1f) / 2f * 0.4f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(star.x * size.width, star.y * star.y * size.height)
            )
        }
    }
}

data class StarData(val x: Float, val y: Float, val size: Float, val alphaSpeed: Float)

@Composable
fun ElvenDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(24.dp)) {
        val midY = size.height / 2
        val midX = size.width / 2
        
        // Central star/gem
        drawCircle(
            color = CelestialBlue,
            radius = 4.dp.toPx(),
            center = Offset(midX, midY)
        )
        
        // Whispy elven lines
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, midY)
            quadraticBezierTo(midX / 2, midY - 10.dp.toPx(), midX - 10.dp.toPx(), midY)
            moveTo(size.width, midY)
            quadraticBezierTo(size.width - midX / 2, midY - 10.dp.toPx(), midX + 10.dp.toPx(), midY)
        }
        
        drawPath(
            path = path,
            color = CelestialBlue.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}
