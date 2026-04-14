package com.imladris.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * IMLADRIS Ethereal Palette
 * A sophisticated, calm, and majestic palette inspired by Rivendell.
 * Removed harsh yellows in favor of Silver, Celestial Blue, and Moonlight.
 */

// Deep Foundations
val MidnightBlue = Color(0xFF0D1117)
val SoftBlack = Color(0xFF010409)
val DeepMist = Color(0xFF161B22)

// Celestial Accents
val SilverGlow = Color(0xFFC9D1D9)      // Main text and iconic elements
val Moonlight = Color(0xFFF0F6FC)       // Highlighted text
val CelestialBlue = Color(0xFF58A6FF)   // Primary action and focus
val EtherealTeal = Color(0xFF79C0FF)    // Subtle variation of blue
val EmeraldHint = Color(0xFF3FB950)     // Nature's presence

// Elegant Warmth (Replacing harsh yellow with a very pale, muted champagne)
val Champagne = Color(0xFFF0E68C).copy(alpha = 0.5f)
val SoftGold = Champagne // For compatibility, but much more muted

// UI States
val FocusGlow = CelestialBlue.copy(alpha = 0.25f)
val ActiveHighlight = SilverGlow.copy(alpha = 0.15f)

// Glassmorphism & Ethereal Effects
val GlassBackground = Color(0x1AFFFFFF)
val GlassBorder = Color(0x22FFFFFF)
val EtherealOverlay = Color(0x0858A6FF)
