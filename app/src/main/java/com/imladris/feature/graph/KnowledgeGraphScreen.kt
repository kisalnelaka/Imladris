package com.imladris.feature.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.core.domain.math.GraphNode
import com.imladris.core.domain.math.Vec2
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.*
import kotlinx.coroutines.isActive

@Composable
fun KnowledgeGraphScreen(
    onArtifactClick: (String, String) -> Unit,
    viewModel: KnowledgeGraphViewModel = hiltViewModel()
) {
    val nodes by viewModel.nodes.collectAsState()
    val edges by viewModel.edges.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val associatedArtifact by viewModel.associatedArtifact.collectAsState()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var canvasSize by remember { mutableStateOf(IntSize(1000, 1000)) }

    // Run physics simulation steps
    LaunchedEffect(canvasSize) {
        while (isActive) {
            viewModel.stepPhysics(canvasSize.width.toFloat(), canvasSize.height.toFloat())
            kotlinx.coroutines.delay(16) // ~60fps integration
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBlack)
            .onSizeChanged { canvasSize = it }
    ) {
        if (nodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No scrolls or sanctuaries mapped yet.\nAdd artifacts to kindle the Knowledge Graph.",
                    color = SilverGlow.copy(alpha = 0.4f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            var draggedNodeId by remember { mutableStateOf<String?>(null) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(nodes) {
                        detectTapGestures { tapOffset ->
                            // Transform tap back to simulation coordinate space
                            val simX = (tapOffset.x - offset.x) / scale
                            val simY = (tapOffset.y - offset.y) / scale
                            val hitNode = nodes.find { node ->
                                val dx = node.position.x - simX
                                val dy = node.position.y - simY
                                (dx * dx + dy * dy) <= (node.radius + 16f) * (node.radius + 16f)
                            }
                            viewModel.selectNode(hitNode)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.4f, 3.5f)
                            offset += pan
                        }
                    }
            ) {
                val nodeMap = nodes.associateBy { it.id }

                // 1. Draw Edges with Hooke spring representation
                edges.forEach { edge ->
                    val src = nodeMap[edge.sourceId]
                    val dst = nodeMap[edge.targetId]
                    if (src != null && dst != null) {
                        val start = Offset(src.position.x * scale + offset.x, src.position.y * scale + offset.y)
                        val end = Offset(dst.position.x * scale + offset.x, dst.position.y * scale + offset.y)

                        drawLine(
                            color = CelestialBlue.copy(alpha = 0.18f),
                            start = start,
                            end = end,
                            strokeWidth = 1.2.dp.toPx() * scale
                        )
                    }
                }

                // 2. Draw Nodes
                nodes.forEach { node ->
                    val center = Offset(node.position.x * scale + offset.x, node.position.y * scale + offset.y)
                    val r = node.radius * scale

                    if (node.isFolder) {
                        // Gateway Node: concentric ethereal rings
                        drawCircle(
                            color = EtherealTeal.copy(alpha = 0.12f),
                            radius = r * 1.6f,
                            center = center
                        )
                        drawCircle(
                            color = EtherealTeal.copy(alpha = 0.8f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 2.dp.toPx() * scale)
                        )
                        drawCircle(
                            color = SoftBlack,
                            radius = r - 2f,
                            center = center
                        )
                    } else {
                        // Book Artifact Node: glow based on progress
                        val isFinished = node.progress >= 0.95f
                        val nodeColor = when {
                            isFinished -> SoftGold
                            node.progress > 0.05f -> EtherealTeal
                            else -> SilverGlow.copy(alpha = 0.6f)
                        }

                        // Outer halo
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.2f),
                            radius = r * 1.5f,
                            center = center
                        )

                        // Core circle
                        drawCircle(
                            color = nodeColor,
                            radius = r,
                            center = center
                        )
                    }
                }
            }
        }

        // Header Title
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "MIND PALACE",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    shadow = Shadow(color = CelestialBlue, blurRadius = 8f)
                ),
                color = SilverGlow
            )
            Text(
                text = "${nodes.count { !it.isFolder }} scrolls in equilibrium (${nodes.count { it.isFolder }} realms)",
                style = MaterialTheme.typography.bodyLarge,
                color = SilverGlow.copy(alpha = 0.5f)
            )
        }

        // Selected Node Inspection Modal / Card
        AnimatedVisibility(
            visible = selectedNode != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            selectedNode?.let { node ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (node.isFolder) Icons.Default.Folder else Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = if (node.isFolder) EtherealTeal else SoftGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (node.isFolder) "SANCTUARY REALM" else "ARTIFACT SCROLL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = CelestialBlue
                                )
                            }
                            IconButton(
                                onClick = { viewModel.selectNode(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = SilverGlow)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = SilverGlow,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!node.isFolder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val pct = (node.progress * 100).toInt()
                            Text(
                                text = "Reading Progress: $pct%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EtherealTeal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = node.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = EtherealTeal,
                                trackColor = DeepMist
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val artifact = associatedArtifact
                                    if (artifact != null) {
                                        onArtifactClick(artifact.title, artifact.path)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CelestialBlue,
                                    contentColor = MidnightBlue
                                ),
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Scroll in Sanctuary", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
