package com.imladris.feature.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.imladris.core.di.WidgetEntryPoint
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.CelestialBlue
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class ImladrisWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).libraryRepository()

        val recentArtifact = repository.getRecentArtifacts().first().firstOrNull()
        val currentBook = recentArtifact?.title ?: "No active journeys"
        val progress = recentArtifact?.progress ?: 0.0f

        provideContent {
            GlanceThemeContent(currentBook, progress)
        }
    }

    @Composable
    private fun GlanceThemeContent(bookTitle: String, progress: Float) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(MidnightBlue))
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Text(
                text = "SANCTUARY",
                style = TextStyle(
                    color = ColorProvider(CelestialBlue),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = bookTitle,
                style = TextStyle(
                    color = ColorProvider(SilverGlow),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            
            if (progress > 0) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(ColorProvider(SilverGlow.copy(alpha = 0.1f)))
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth() // Simplified for production widget
                            .height(2.dp)
                            .background(ColorProvider(CelestialBlue))
                    ) {}
                }
            }
        }
    }
}

class ImladrisWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ImladrisWidget()
}
