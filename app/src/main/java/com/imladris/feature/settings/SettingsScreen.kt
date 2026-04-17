package com.imladris.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imladris.core.ui.components.ElvenDivider
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.components.MajesticBackground
import com.imladris.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    MajesticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = SilverGlow)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Library Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SilverGlow
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            ElvenDivider()
            Spacer(modifier = Modifier.height(32.dp))
            
            SettingsSection("Display") {
                SettingsToggle("Dark Mode", true) {}
                SettingsToggle("High Contrast Text", false) {}
            }
            
            SettingsSection("Storage") {
                SettingsAction("Clear Cache", "Remove temporary PDF manifests") {}
                SettingsAction("Rescan Library", "Force refresh all sanctuaries") {}
            }
            
            SettingsSection("About") {
                Text(
                    "Imladris v1.2\nAn offline-first majestic library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverGlow.copy(alpha = 0.5f),
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Champagne,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = SilverGlow)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CelestialBlue)
        )
    }
}

@Composable
fun SettingsAction(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = SilverGlow)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SilverGlow.copy(alpha = 0.4f))
    }
}
