package com.imladris.feature.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

@Composable
fun AnalyticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Knowledge Insights",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            FocusScoreCard(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StreakCard(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Reading Timeline",
            style = MaterialTheme.typography.headlineMedium,
            color = SoftGold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TimelineList()
    }
}

@Composable
fun FocusScoreCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Text(text = "Focus Score", color = SilverGlow.copy(alpha = 0.6f))
        Text(text = "88", style = MaterialTheme.typography.displayLarge, color = SoftGold)
    }
}

@Composable
fun StreakCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Text(text = "Reading Streak", color = SilverGlow.copy(alpha = 0.6f))
        Text(text = "12 Days", style = MaterialTheme.typography.displayLarge, color = SilverGlow)
    }
}

@Composable
fun TimelineList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TimelineItem("Today", "Completed 'The Hobbit' chapter 5")
        TimelineItem("Yesterday", "Resurfaced 3 memories from 'The Silmarillion'")
        TimelineItem("3 Days ago", "Connected 'Philosophy' to 'Ancient Lore'")
    }
}

@Composable
fun TimelineItem(time: String, action: String) {
    Row {
        Text(text = time, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(80.dp))
        Text(text = action, style = MaterialTheme.typography.bodyLarge)
    }
}
