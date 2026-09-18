package com.imladris.core.domain.math

import com.imladris.core.data.local.entities.ArtifactEntity
import org.junit.Assert.*
import org.junit.Test

class MathematicalEnginesTest {

    @Test
    fun testForceDirectedGraphEngineConvergence() {
        val engine = ForceDirectedGraphEngine()
        val nodeA = GraphNode("1", "Fellowship", false, null, 0.5f, 20f, Vec2(100f, 100f))
        val nodeB = GraphNode("2", "Two Towers", false, null, 0.2f, 20f, Vec2(110f, 105f))
        val edge = GraphEdge("1", "2", 1.0f, 150f)

        engine.setGraph(listOf(nodeA, nodeB), listOf(edge))

        val initialDistance = nodeA.position.distanceTo(nodeB.position)
        assertEquals(11.18f, initialDistance, 0.1f)

        // Run 50 physics steps
        for (i in 0 until 50) {
            engine.step(1000f, 1000f, 0.016f)
        }

        val evolvedDistance = nodeA.position.distanceTo(nodeB.position)
        // Nodes started at dist ~11 with target 150 and strong Coulomb repulsion; distance must increase towards equilibrium
        assertTrue("Nodes must repel towards spring equilibrium: evolved=$evolvedDistance", evolvedDistance > initialDistance)
    }

    @Test
    fun testReadingPacingEngineWpmAndEstimates() {
        val initialWpm = 200f
        // Read 500 words in 120 seconds = 250 WPM instant
        val updatedWpm = ReadingPacingEngine.updateWpm(initialWpm, 500, 120f)
        // Alpha is 0.25 -> 0.25 * 250 + 0.75 * 200 = 62.5 + 150 = 212.5 WPM
        assertEquals(212.5f, updatedWpm, 0.5f)

        val minutesRemaining = ReadingPacingEngine.estimateMinutesRemaining(2125, updatedWpm)
        assertEquals(10, minutesRemaining)
    }

    @Test
    fun testBionicSaccadicConversion() {
        val tokens = ReadingPacingEngine.toBionicTokens("Rivendell knowledge sanctuary")
        assertEquals(3, tokens.size)
        // "Rivendell" (len 9) -> split at 3
        assertEquals("Riv", tokens[0].prefix)
        assertEquals("endell ", tokens[0].suffix)
        // "knowledge" (len 9) -> split at 3
        assertEquals("kno", tokens[1].prefix)
        assertEquals("wledge ", tokens[1].suffix)
    }

    @Test
    fun testPaginationAlgorithm() {
        val sampleText = "Chapter One.\n\n" + "The quick brown fox jumps over the lazy dog. ".repeat(40) +
                "\n\nChapter Two.\n\n" + "And the journey through Imladris continues. ".repeat(40)

        val pages = ReadingPacingEngine.paginateText(sampleText, 360f, 640f, 18f)
        assertTrue("Text must be paginated into multiple pages", pages.size >= 2)
        assertFalse("Pages must not be empty", pages.any { it.isBlank() })
    }

    @Test
    fun testRecommendationEngineScoring() {
        val now = System.currentTimeMillis()
        val bookA = ArtifactEntity(
            id = "a", title = "Active Journey", path = "path/a", type = "txt",
            coverPath = null, lastRead = now - 10000L, addedDate = now - 500000L,
            progress = 0.52f, parentFolderId = null
        )
        val bookB = ArtifactEntity(
            id = "b", title = "Forgotten Tome", path = "path/b", type = "txt",
            coverPath = null, lastRead = now - 15 * 86400000L, addedDate = now - 30 * 86400000L,
            progress = 0.40f, parentFolderId = null
        )
        val bookC = ArtifactEntity(
            id = "c", title = "Fresh Scroll", path = "path/c", type = "txt",
            coverPath = null, lastRead = 0L, addedDate = now,
            progress = 0.0f, parentFolderId = null
        )

        val bundle = RecommendationEngine.computeRecommendations(listOf(bookA, bookB, bookC), now)
        assertNotNull(bundle.primary)
        assertEquals("a", bundle.primary?.artifact?.id)
        assertEquals("Current Resonance", bundle.primary?.categoryTitle)

        assertNotNull(bundle.secondary)
        assertEquals("b", bundle.secondary?.artifact?.id)
        assertEquals("Forgotten Lore", bundle.secondary?.categoryTitle)
    }
}
