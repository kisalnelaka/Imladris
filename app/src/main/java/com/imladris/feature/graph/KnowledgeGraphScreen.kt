package com.imladris.feature.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.ui.theme.*
import kotlin.random.Random

@Composable
fun KnowledgeGraphScreen(
    viewModel: KnowledgeGraphViewModel = hiltViewModel()
) {
    val artifacts by viewModel.artifacts.collectAsState(initial = emptyList())
    
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBlack)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
    ) {
        if (artifacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No artifacts to map.", color = SilverGlow.copy(alpha = 0.3f))
            }
        } else {
            val nodes = remember(artifacts) {
                artifacts.map { 
                    Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 1500f) 
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw connections
                nodes.forEachIndexed { index, start ->
                    if (index < nodes.size - 1) {
                        val end = nodes[index + 1]
                        drawLine(
                            color = CelestialBlue.copy(alpha = 0.15f),
                            start = (start * scale) + offset,
                            end = (end * scale) + offset,
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Draw nodes (Books)
                nodes.forEachIndexed { index, node ->
                    val artifact = artifacts[index]
                    val center = (node * scale) + offset
                    
                    drawCircle(
                        color = if (artifact.progress > 0) EtherealTeal else SilverGlow.copy(alpha = 0.5f),
                        radius = (10.dp.toPx() + (artifact.progress * 15.dp.toPx())) * scale,
                        center = center
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Knowledge Graph",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    shadow = Shadow(color = CelestialBlue, blurRadius = 8f)
                ),
                color = SilverGlow
            )
            Text(
                text = "${artifacts.size} artifacts connected in your mind palace",
                style = MaterialTheme.typography.bodyLarge,
                color = SilverGlow.copy(alpha = 0.5f)
            )
        }
    }
}
