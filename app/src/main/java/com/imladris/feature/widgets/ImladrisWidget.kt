package com.imladris.feature.widgets

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

class ImladrisWidget : GlanceAppWidget() {
    override suspend fun provideContent(context: Context, id: GlanceId) {
        // In a real app, fetch data from Room or DataStore
        val currentBook = "The Fellowship of the Ring"
        val progress = 0.45f

        provideContent {
            GlanceThemeContent(currentBook, progress)
        }
    }

    @androidx.compose.runtime.Composable
    private fun GlanceThemeContent(bookTitle: String, progress: Float) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(MidnightBlue))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "CONINUE READING",
                style = TextStyle(
                    color = ColorProvider(SoftGold),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = bookTitle,
                style = TextStyle(
                    color = ColorProvider(SilverGlow),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            // Minimal progress bar representation
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(SilverGlow.copy(alpha = 0.2f)))
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(ColorProvider(SoftGold))
                )
            }
        }
    }
}

class ImladrisWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ImladrisWidget()
}
