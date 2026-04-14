package com.imladris.feature.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontStyle
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.imladris.core.di.WidgetEntryPoint
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.CelestialBlue
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class MemoryRecallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).libraryRepository()

        // Fetch a real random highlight if possible
        val artifacts = database.getRecentArtifacts().first()
        var quote = "The sanctuary awaits your first reflection."
        var source = "Imladris"

        if (artifacts.isNotEmpty()) {
            val artifact = artifacts.random()
            // Note: In a full implementation, we'd fetch actual highlights here
            // For now, we use the book title as the 'source' if highlights are empty
            quote = "Continuing your journey in this artifact..."
            source = artifact.title
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(MidnightBlue))
                    .padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "A MEMORY",
                    style = TextStyle(
                        color = ColorProvider(CelestialBlue),
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "\"$quote\"",
                    style = TextStyle(
                        color = ColorProvider(SilverGlow),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "- $source",
                    style = TextStyle(
                        color = ColorProvider(SilverGlow.copy(alpha = 0.6f)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
