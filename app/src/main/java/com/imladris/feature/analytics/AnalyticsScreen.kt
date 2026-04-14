package com.imladris.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val artifactCount by viewModel.artifactCount.collectAsState(initial = 0)
    val highlightCount by viewModel.highlightCount.collectAsState(initial = 0)
    val focusScore by viewModel.focusScore.collectAsState(initial = 0)
    val recentArtifacts by viewModel.recentArtifacts.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Knowledge Insights",
            style = MaterialTheme.typography.displayLarge,
            color = SilverGlow
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            InsightCard(
                title = "Focus Score",
                value = "$focusScore",
                modifier = Modifier.weight(1f),
                color = SoftGold
            )
            Spacer(modifier = Modifier.width(16.dp))
            InsightCard(
                title = "Artifacts",
                value = "$artifactCount",
                modifier = Modifier.weight(1f),
                color = CelestialBlue
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Reading History",
            style = MaterialTheme.typography.headlineMedium,
            color = Champagne
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (recentArtifacts.isEmpty()) {
            Text("No recent activity found.", color = SilverGlow.copy(alpha = 0.4f))
        } else {
            recentArtifacts.forEach { artifact ->
                TimelineItem(artifact)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun InsightCard(title: String, value: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color) {
    GlassCard(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = SilverGlow.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.displayMedium, color = color)
    }
}

@Composable
fun TimelineItem(artifact: ArtifactEntity) {
    val date = remember(artifact.lastRead) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(artifact.lastRead))
    }
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = artifact.title, style = MaterialTheme.typography.bodyLarge, color = SilverGlow, maxLines = 1)
            Text(text = date, style = MaterialTheme.typography.labelSmall, color = SilverGlow.copy(alpha = 0.4f))
        }
        Text(
            text = "${(artifact.progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = CelestialBlue
        )
    }
}
