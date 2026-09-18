package com.imladris.core.domain.math

import com.imladris.core.data.local.entities.ArtifactEntity
import kotlin.math.exp

data class ScoredRecommendation(
    val artifact: ArtifactEntity,
    val score: Float,
    val categoryTitle: String,
    val rationale: String
)

data class RecommendationBundle(
    val primary: ScoredRecommendation?,
    val secondary: ScoredRecommendation?
)

/**
 * Pure Kotlin Multi-Factor Recommendation Engine.
 * Evaluates recency decay, reading readiness, and intellectual momentum.
 */
object RecommendationEngine {

    private const val DECAY_LAMBDA = 0.08f // ~50% retention score after 8.6 days
    private const val MILLIS_PER_DAY = 86_400_000f

    /**
     * Scores all artifacts and extracts the top two resonant recommendations.
     */
    fun computeRecommendations(
        allArtifacts: List<ArtifactEntity>,
        currentTime: Long = System.currentTimeMillis()
    ): RecommendationBundle {
        if (allArtifacts.isEmpty()) {
            return RecommendationBundle(null, null)
        }

        val inProgress = allArtifacts.filter { it.progress in 0.02f..0.98f }
        val unread = allArtifacts.filter { it.progress < 0.02f }

        // 1. Find Primary: Current Resonance (most active or highest momentum in-progress book)
        val primary = if (inProgress.isNotEmpty()) {
            val bestInProgress = inProgress.maxByOrNull { artifact ->
                val daysSinceRead = (currentTime - artifact.lastRead).coerceAtLeast(0L) / MILLIS_PER_DAY
                val recency = exp(-DECAY_LAMBDA * daysSinceRead)
                // Inverted parabolic readiness peak around 50%
                val readiness = 1.0f - 4.0f * (artifact.progress - 0.5f) * (artifact.progress - 0.5f)
                (recency * 0.6f + readiness * 0.4f)
            }
            bestInProgress?.let {
                val pct = (it.progress * 100).toInt()
                ScoredRecommendation(
                    artifact = it,
                    score = 1.0f,
                    categoryTitle = "Current Resonance",
                    rationale = "$pct% completed • Resume your journey"
                )
            }
        } else {
            // Fallback to most recently opened or added
            val fallback = allArtifacts.maxByOrNull { maxOf(it.lastRead, it.addedDate) }
            fallback?.let {
                ScoredRecommendation(
                    artifact = it,
                    score = 0.8f,
                    categoryTitle = "Sanctuary Scroll",
                    rationale = "Explore this archive"
                )
            }
        }

        // 2. Find Secondary: Either "Forgotten Lore" (started long ago) or "Sanctuary Discovery" (fresh unread)
        val secondary = if (inProgress.size > 1) {
            // Check for forgotten in-progress scrolls
            val forgotten = inProgress
                .filter { it.id != primary?.artifact?.id }
                .maxByOrNull { currentTime - it.lastRead }

            forgotten?.let {
                val days = ((currentTime - it.lastRead) / MILLIS_PER_DAY).toInt()
                ScoredRecommendation(
                    artifact = it,
                    score = 0.75f,
                    categoryTitle = "Forgotten Lore",
                    rationale = "Left off ${days}d ago at ${(it.progress * 100).toInt()}%"
                )
            }
        } else if (unread.isNotEmpty()) {
            val fresh = unread.maxByOrNull { it.addedDate }
            fresh?.let {
                ScoredRecommendation(
                    artifact = it,
                    score = 0.7f,
                    categoryTitle = "New Discovery",
                    rationale = "Unread scroll waiting in archives"
                )
            }
        } else {
            allArtifacts.firstOrNull { it.id != primary?.artifact?.id }?.let {
                ScoredRecommendation(
                    artifact = it,
                    score = 0.6f,
                    categoryTitle = "Archive Echo",
                    rationale = "Reflect upon this scroll"
                )
            }
        }

        return RecommendationBundle(primary, secondary)
    }

    /**
     * Calculates Focus Momentum Score (0 - 100).
     */
    fun calculateFocusResonance(
        artifacts: List<ArtifactEntity>,
        totalMinutesRead: Int
    ): Int {
        if (artifacts.isEmpty()) return 0
        val activeCount = artifacts.count { it.progress > 0.05f }
        val avgProgress = artifacts.map { it.progress }.average().toFloat()
        val volumeScore = (totalMinutesRead / 30f).coerceIn(0f, 40f)
        val completionScore = (avgProgress * 35f).coerceIn(0f, 35f)
        val breadthScore = (activeCount * 5f).coerceIn(0f, 25f)
        return (volumeScore + completionScore + breadthScore).toInt().coerceIn(0, 100)
    }
}
