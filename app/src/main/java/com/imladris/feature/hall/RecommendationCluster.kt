package com.imladris.feature.hall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.imladris.core.domain.math.RecommendationBundle
import com.imladris.core.domain.math.ScoredRecommendation
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.*

@Composable
fun RecommendationCluster(
    recommendationBundle: RecommendationBundle,
    onArtifactClick: (String, String) -> Unit
) {
    val primary = recommendationBundle.primary
    val secondary = recommendationBundle.secondary

    if (primary == null && secondary == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SoftGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SANCTUARY RESONANCES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = SoftGold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (primary != null) {
                Box(modifier = Modifier.weight(1f)) {
                    ResonanceCard(primary, onArtifactClick)
                }
            }
            if (secondary != null) {
                Box(modifier = Modifier.weight(1f)) {
                    ResonanceCard(secondary, onArtifactClick)
                }
            }
        }
    }
}

@Composable
private fun ResonanceCard(
    recommendation: ScoredRecommendation,
    onArtifactClick: (String, String) -> Unit
) {
    val artifact = recommendation.artifact
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp)
            .clickable { onArtifactClick(artifact.title, artifact.path) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recommendation.categoryTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = CelestialBlue
                )
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = SilverGlow.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (artifact.coverPath != null) {
                        AsyncImage(
                            model = artifact.coverPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = artifact.title.take(1).uppercase(),
                            color = SilverGlow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artifact.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = SilverGlow,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recommendation.rationale,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CelestialBlue.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (artifact.progress > 0f) {
                LinearProgressIndicator(
                    progress = artifact.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = EtherealTeal,
                    trackColor = DeepMist.copy(alpha = 0.5f)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
