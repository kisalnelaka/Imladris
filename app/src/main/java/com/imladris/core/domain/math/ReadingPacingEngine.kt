package com.imladris.core.domain.math

import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * Encapsulates a bionic token: bold prefix for fixation saccade, remainder for visual continuity.
 */
data class BionicToken(
    val prefix: String,
    val suffix: String
)

/**
 * Encapsulates circadian color palette for comfortable reading across all hours of the day.
 */
data class CircadianTheme(
    val background: Color,
    val surface: Color,
    val text: Color,
    val accent: Color,
    val name: String
)

/**
 * Pure Kotlin mathematical reading pacing, bionic saccades, and text pagination engine.
 */
object ReadingPacingEngine {

    private const val DEFAULT_WPM = 220f
    private const val ALPHA = 0.25f // Smoothing factor for Exponential Moving Average

    /**
     * Updates reading speed using Exponential Moving Average (EMA).
     * Prevents erratic spikes while smoothly adapting to the reader's actual cadence.
     */
    fun updateWpm(previousWpm: Float, wordsRead: Int, durationSeconds: Float): Float {
        if (durationSeconds < 2f || wordsRead <= 0) return previousWpm
        val instantWpm = (wordsRead / durationSeconds) * 60f
        // Clamp instant WPM to realistic human bounds
        val clampedInstant = instantWpm.coerceIn(80f, 900f)
        val baseline = if (previousWpm <= 0f) DEFAULT_WPM else previousWpm
        return (ALPHA * clampedInstant + (1f - ALPHA) * baseline).coerceIn(80f, 900f)
    }

    /**
     * Estimates minutes remaining based on current reading speed.
     */
    fun estimateMinutesRemaining(wordsRemaining: Int, wpm: Float): Int {
        val speed = if (wpm > 50f) wpm else DEFAULT_WPM
        val minutes = ceil(wordsRemaining.toFloat() / speed).toInt()
        return max(1, minutes)
    }

    /**
     * Converts a raw text string into Bionic Saccadic tokens.
     * Bolds the initial 35-45% of each word to create optical fixation points
     * that guide the eye during ADHD or high-speed reading sessions.
     */
    fun toBionicTokens(text: String): List<BionicToken> {
        val words = text.split(" ")
        return words.map { word ->
            if (word.isEmpty()) {
                BionicToken("", "")
            } else {
                val cleanWord = word.trim()
                val len = cleanWord.length
                val splitIdx = when {
                    len <= 1 -> 1
                    len <= 3 -> 1
                    len <= 6 -> 2
                    len <= 9 -> 3
                    else -> ceil(len * 0.4f).toInt()
                }.coerceIn(1, len)

                BionicToken(
                    prefix = cleanWord.substring(0, splitIdx),
                    suffix = cleanWord.substring(splitIdx) + " "
                )
            }
        }
    }

    /**
     * Mathematical pagination algorithm: breaks a long stream of text into discrete
     * pages based on estimated characters per page, respecting paragraph and sentence boundaries.
     */
    fun paginateText(
        fullText: String,
        viewportWidthDp: Float,
        viewportHeightDp: Float,
        fontSizeSp: Float
    ): List<String> {
        if (fullText.isBlank()) return listOf("Empty scroll.")

        // Approximate character budget per page based on geometry
        // Average char width in monospace or serif is ~0.55 of fontSizeSp
        val usableWidth = max(240f, viewportWidthDp - 48f)
        val usableHeight = max(320f, viewportHeightDp - 140f)
        val charsPerLine = max(25, (usableWidth / (fontSizeSp * 0.55f)).toInt())
        val linesPerPage = max(10, (usableHeight / (fontSizeSp * 1.65f)).toInt())
        val charsPerPage = max(300, charsPerLine * linesPerPage)

        val pages = mutableListOf<String>()
        val paragraphs = fullText.split("\n\n")
        val currentPage = StringBuilder()

        for (paragraph in paragraphs) {
            val trimmedPara = paragraph.trim()
            if (trimmedPara.isEmpty()) continue

            if (currentPage.length + trimmedPara.length + 2 <= charsPerPage) {
                if (currentPage.isNotEmpty()) currentPage.append("\n\n")
                currentPage.append(trimmedPara)
            } else {
                // If the single paragraph itself exceeds charsPerPage, chunk it by sentences
                if (trimmedPara.length > charsPerPage) {
                    val sentences = trimmedPara.split(Regex("(?<=[.!?])\\s+"))
                    for (sentence in sentences) {
                        if (currentPage.length + sentence.length + 1 <= charsPerPage) {
                            if (currentPage.isNotEmpty()) currentPage.append(" ")
                            currentPage.append(sentence)
                        } else {
                            if (currentPage.isNotEmpty()) {
                                pages.add(currentPage.toString())
                                currentPage.clear()
                            }
                            currentPage.append(sentence)
                        }
                    }
                } else {
                    if (currentPage.isNotEmpty()) {
                        pages.add(currentPage.toString())
                        currentPage.clear()
                    }
                    currentPage.append(trimmedPara)
                }
            }
        }

        if (currentPage.isNotEmpty()) {
            pages.add(currentPage.toString())
        }

        return if (pages.isEmpty()) listOf(fullText) else pages
    }

    /**
     * Computes Circadian color adaptation based on current 24h clock.
     * Generates ideal eye comfort contrast curves for:
     * - Dawn Mist (5 - 8)
     * - Rivendell Daylight (8 - 18)
     * - Golden Twilight (18 - 21)
     * - Obsidian Midnight Sanctuary (21 - 5)
     */
    fun getCircadianTheme(hourOfDay: Int): CircadianTheme {
        return when (hourOfDay) {
            in 5..7 -> CircadianTheme(
                background = Color(0xFF1A1C23),
                surface = Color(0xFF262933),
                text = Color(0xFFE2E4EC),
                accent = Color(0xFF8AA2D6),
                name = "Dawn Mist"
            )
            in 8..17 -> CircadianTheme(
                background = Color(0xFFF9F7F1),
                surface = Color(0xFFFFFFFF),
                text = Color(0xFF2B2D42),
                accent = Color(0xFF2E6F9E),
                name = "Rivendell Daylight"
            )
            in 18..20 -> CircadianTheme(
                background = Color(0xFF221F1E),
                surface = Color(0xFF2E2927),
                text = Color(0xFFF0EBE1),
                accent = Color(0xFFD4A373),
                name = "Golden Twilight"
            )
            else -> CircadianTheme(
                background = Color(0xFF0D0F14),
                surface = Color(0xFF161922),
                text = Color(0xFFD4D8E2),
                accent = Color(0xFF79C0FF),
                name = "Obsidian Midnight"
            )
        }
    }
}
