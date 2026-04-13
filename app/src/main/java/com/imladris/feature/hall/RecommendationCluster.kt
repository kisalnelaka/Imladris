package com.imladris.feature.hall

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.SilverGlow

@Composable
fun RecommendationCluster() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            GlassCard {
                Text(text = "The Lost Tales", color = SilverGlow)
                Text(
                    text = "Based on your interest in History",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            GlassCard {
                Text(text = "Unfinished Lore", color = SilverGlow)
                Text(
                    text = "You left off on page 42",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
