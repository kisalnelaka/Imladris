package com.imladris.feature.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.ui.components.ElvenDivider
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.components.MajesticBackground
import com.imladris.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val artifactCount by viewModel.artifactCount.collectAsState(initial = 0)
    val focusScore by viewModel.focusScore.collectAsState(initial = 0)
    val recentArtifacts by viewModel.recentArtifacts.collectAsState(initial = emptyList())

    MajesticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "CHRONICLE",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                    fontSize = 44.sp,
                    shadow = Shadow(color = CelestialBlue.copy(alpha = 0.5f), blurRadius = 12f)
                ),
                color = SilverGlow
            )
            
            Text(
                text = "The records of your intellectual journey",
                style = MaterialTheme.typography.bodyLarge,
                color = CelestialBlue.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))
            ElvenDivider()
            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                MajesticInsightCard(
                    title = "FOCUS RESONANCE",
                    value = "$focusScore",
                    modifier = Modifier.weight(1f),
                    color = SoftGold
                )
                Spacer(modifier = Modifier.width(16.dp))
                MajesticInsightCard(
                    title = "SCROLLS INDEXED",
                    value = "$artifactCount",
                    modifier = Modifier.weight(1f),
                    color = CelestialBlue
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "RECENT PATHS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Champagne
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (recentArtifacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No records found in the library archives.", color = SilverGlow.copy(alpha = 0.3f))
                }
            } else {
                recentArtifacts.forEach { artifact ->
                    MajesticTimelineItem(artifact)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun MajesticInsightCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, 
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), 
                color = SilverGlow.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.displayMedium.copy(
                    shadow = Shadow(color = color.copy(alpha = glowAlpha), blurRadius = 16f)
                ), 
                color = color
            )
        }
    }
}

@Composable
fun MajesticTimelineItem(artifact: ArtifactEntity) {
    val date = remember(artifact.lastRead) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(artifact.lastRead))
    }
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(CelestialBlue, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artifact.title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = SilverGlow, 
                    maxLines = 1
                )
                Text(
                    text = "Reflected upon at $date", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = SilverGlow.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "${(artifact.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = CelestialBlue
            )
        }
    }
}
