package com.imladris.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CelestialBlue,
    secondary = SilverGlow,
    tertiary = EmeraldHint,
    background = MidnightBlue,
    surface = DeepMist,
    onPrimary = MidnightBlue,
    onSecondary = MidnightBlue,
    onTertiary = Moonlight,
    onBackground = Moonlight,
    onSurface = Moonlight
)

private val LightColorScheme = lightColorScheme(
    primary = CelestialBlue,
    secondary = DeepMist,
    tertiary = EmeraldHint,
    background = Moonlight,
    surface = SilverGlow,
    onPrimary = Moonlight,
    onSecondary = Moonlight,
    onTertiary = MidnightBlue,
    onBackground = MidnightBlue,
    onSurface = MidnightBlue
)

@Composable
fun ImladrisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
