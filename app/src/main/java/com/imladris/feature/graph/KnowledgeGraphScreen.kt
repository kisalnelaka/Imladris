package com.imladris.feature.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.imladris.core.ui.theme.EtherealTeal
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

data class GraphNode(val id: String, val label: String, var position: Offset)
data class GraphEdge(val fromId: String, val toId: String)

@Composable
fun KnowledgeGraphScreen() {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    
    val nodes = remember {
        listOf(
            GraphNode("1", "The Hobbit", Offset(300f, 400f)),
            GraphNode("2", "The Silmarillion", Offset(600f, 300f)),
            GraphNode("3", "Unfinished Tales", Offset(500f, 600f)),
            GraphNode("4", "Lost Road", Offset(800f, 500f))
        )
    }
    
    val edges = remember {
        listOf(
            GraphEdge("1", "2"),
            GraphEdge("2", "3"),
            GraphEdge("3", "4"),
            GraphEdge("2", "4")
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    offset += pan
                    zoom *= gestureZoom
                }
            }
    ) {
        // Draw Edges (Links)
        edges.forEach { edge ->
            val from = nodes.find { it.id == edge.fromId }?.position ?: Offset.Zero
            val to = nodes.find { it.id == edge.toId }?.position ?: Offset.Zero
            
            drawLine(
                color = EtherealTeal.copy(alpha = 0.3f),
                start = (from + offset) * zoom,
                end = (to + offset) * zoom,
                strokeWidth = 2f
            )
        }

        // Draw Nodes (Artifacts)
        nodes.forEach { node ->
            val pos = (node.position + offset) * zoom
            
            // Outer glow
            drawCircle(
                color = SoftGold.copy(alpha = 0.2f),
                radius = 40f * zoom,
                center = pos
            )
            
            // Core node
            drawCircle(
                color = SoftGold,
                radius = 10f * zoom,
                center = pos,
                style = Stroke(width = 2f)
            )
            
            // We could add text here using native canvas text or a specialized helper
        }
    }
}
