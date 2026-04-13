package com.imladris.feature.hall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imladris.core.ui.components.EtherealGlow
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

@Composable
fun HallOfImladrisScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Hall of Imladris",
                style = MaterialTheme.typography.displayLarge,
                color = SilverGlow
            )
            Text(
                text = "The sanctuary of your thoughts",
                style = MaterialTheme.typography.bodyLarge,
                color = SilverGlow.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Recent Artifacts",
                style = MaterialTheme.typography.headlineMedium,
                color = SoftGold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RecentArtifactsRow()
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Suggested for Reflection",
                style = MaterialTheme.typography.headlineMedium,
                color = SoftGold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RecommendationCluster()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Gateways",
                style = MaterialTheme.typography.headlineMedium,
                color = SoftGold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GatewaysGrid {}
        }

        // Floating Action Button to initiate SAF Picker
        FloatingActionButton(
            onClick = { /* In real app, launch directory picker */ },
            containerColor = SoftGold,
            contentColor = MidnightBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sanctuary")
        }
    }
}

@Composable
fun RecentArtifactsRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(listOf("Ancient Lore", "The Silmarillion", "Letters from Gondor")) { title ->
            // Use the SpatialEngine wrapper for floating effect
            com.imladris.core.ui.spatial.SpatialArtifact {
                GlassCard(
                    modifier = Modifier.width(160.dp).height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GatewaysGrid(onGatewayClick: (String) -> Unit) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GatewayItem("History", Color(0xFF64FFDA)) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onGatewayClick("History")
        }
        GatewayItem("Philosophy", Color(0xFFD4AF37)) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onGatewayClick("Philosophy")
        }
        GatewayItem("Science", Color(0xFFE1E8ED)) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onGatewayClick("Science")
        }
    }
}

@Composable
fun GatewayItem(name: String, glowColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = androidx.compose.foundation.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        EtherealGlow(color = glowColor) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, glowColor.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        // Subtle inner glow
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(glowColor.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = SilverGlow, fontSize = 14.sp)
    }
}
