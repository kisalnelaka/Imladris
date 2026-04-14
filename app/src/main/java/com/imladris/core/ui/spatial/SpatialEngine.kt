package com.imladris.core.ui.spatial

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A layout that provides a pseudo-3D parallax effect for floating artifacts.
 * Uses sensor data or scroll position to rotate elements slightly.
 */
@Composable
fun SpatialArtifact(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val animatedY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = SineEaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    Box(
        modifier = modifier
            .padding(16.dp)
            .graphicsLayer {
                translationY = animatedY.dp.toPx()
                // Subtle rotation for "ethereal" feel
                rotationZ = animatedY / 4
            }
    ) {
        content()
    }
}

private val SineEaseInOut = Easing { fraction ->
    ((1 - Math.cos(fraction * Math.PI.toDouble())) / 2).toFloat()
}
