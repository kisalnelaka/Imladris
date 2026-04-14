package com.imladris.feature.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.theme.EtherealTeal
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

@Composable
fun KnowledgeGraphScreen() {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw connections (constellations)
            val nodes = listOf(
                Offset(300f, 400f),
                Offset(600f, 700f),
                Offset(200f, 900f),
                Offset(800f, 300f),
                Offset(500f, 200f)
            )

            nodes.forEachIndexed { index, start ->
                nodes.drop(index + 1).forEach { end ->
                    drawLine(
                        color = EtherealTeal.copy(alpha = 0.2f),
                        start = (start * scale) + offset,
                        end = (end * scale) + offset,
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Draw nodes
            nodes.forEach { node ->
                drawCircle(
                    color = SoftGold,
                    radius = 8.dp.toPx() * scale,
                    center = (node * scale) + offset
                )
                drawCircle(
                    color = SoftGold.copy(alpha = 0.3f),
                    radius = 16.dp.toPx() * scale,
                    center = (node * scale) + offset
                )
            }
        }

        Text(
            text = "Knowledge Graph",
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(color = SoftGold, blurRadius = 8f)
            ),
            modifier = Modifier.padding(24.dp)
        )
        
        Text(
            text = "Explore the connections between your thoughts",
            style = MaterialTheme.typography.bodyLarge,
            color = SilverGlow.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 24.dp, top = 64.dp)
        )
    }
}
