package com.imladris.feature.widgets

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontStyle
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

class MemoryRecallWidget : GlanceAppWidget() {
    override suspend fun provideContent(context: Context, id: GlanceId) {
        val quote = "Not all those who wander are lost."
        val source = "J.R.R. Tolkien"

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(MidnightBlue))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A MEMORY",
                    style = TextStyle(
                        color = ColorProvider(SoftGold),
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
                    text = "— $source",
                    style = TextStyle(
                        color = ColorProvider(SilverGlow.copy(alpha = 0.6f)),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
