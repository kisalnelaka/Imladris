package com.imladris.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
                alphaSpeed = Random.nextFloat() * 0.001f + 0.0005f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val time = System.currentTimeMillis()
            val alpha = (Math.sin(time * star.alphaSpeed.toDouble()).toFloat() + 1f) / 2f * 0.4f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(star.x * size.width, star.y * size.height)
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
        
        drawCircle(
            color = CelestialBlue,
            radius = 3.dp.toPx(),
            center = Offset(midX, midY)
        )
        
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(midX - 20.dp.toPx(), midY)
            lineTo(20.dp.toPx(), midY)
            moveTo(midX + 20.dp.toPx(), midY)
            lineTo(size.width - 20.dp.toPx(), midY)
        }
        
        drawPath(
            path = path,
            color = CelestialBlue.copy(alpha = 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    rigidHorizontal: Boolean = false,
    content: @Composable (scale: Float, offset: Offset) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        val newX = if (rigidHorizontal) 0f else offset.x + pan.x
                        offset = Offset(newX, offset.y + pan.y)
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        content(scale, offset)
    }
}

@Composable
fun EtherealBackgroundGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.radialGradient(
                    colors = listOf(CelestialBlue, Color.Transparent),
                    center = Offset(200f, 200f),
                    radius = 800f
                )
            )
    )
}

@Composable
fun ReadingVignette(isFocusMode: Boolean) {
    val alpha by animateFloatAsState(if (isFocusMode) 0.9f else 0.4f, animationSpec = tween(1000), label = "vignette")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.verticalGradient(
                    0f to MidnightBlue,
                    0.15f to Color.Transparent,
                    0.85f to Color.Transparent,
                    1f to MidnightBlue
                )
            )
    )
}
