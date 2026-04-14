package com.imladris.feature.hall

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.R
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.*
import com.imladris.feature.library.LibraryViewModel

@Composable
fun HallOfImladrisScreen(
    onArtifactClick: (String, String) -> Unit,
    hallViewModel: HallViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val recentArtifacts by hallViewModel.recentArtifacts.collectAsState(initial = emptyList())
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { libraryViewModel.scanDirectory(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBlue, SoftBlack)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Sanctuary",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    fontSize = 48.sp
                ),
                color = SilverGlow
            )
            
            Text(
                text = "Where your journey through knowledge begins",
                style = MaterialTheme.typography.bodyLarge,
                color = CelestialBlue.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(56.dp))
            
            if (recentArtifacts.isNotEmpty()) {
                Text(
                    text = "Continue Reading",
                    style = MaterialTheme.typography.titleMedium,
                    color = Champagne,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                RecentArtifactsRow(recentArtifacts, onArtifactClick)
                Spacer(modifier = Modifier.height(48.dp))
            }

            Text(
                text = "Library Overview",
                style = MaterialTheme.typography.titleMedium,
                color = Champagne,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (recentArtifacts.isEmpty()) {
                EmptyLibraryPrompt { launcher.launch(null) }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FolderOpen,
                        title = "Import Files",
                        onClick = { launcher.launch(null) }
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AutoStories,
                        title = "Statistics",
                        onClick = { /* Navigate to analytics */ }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }

        FloatingActionButton(
            onClick = { launcher.launch(null) },
            containerColor = CelestialBlue,
            contentColor = MidnightBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sanctuary", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = CelestialBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title, 
                color = SilverGlow, 
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyLibraryPrompt(onAddClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddClick)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                tint = CelestialBlue,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Awaiting Enlightenment",
                style = MaterialTheme.typography.headlineSmall,
                color = SilverGlow,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select a scroll-directory to fill these halls with wisdom.",
                style = MaterialTheme.typography.bodyMedium,
                color = SilverGlow.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentArtifactsRow(
    artifacts: List<ArtifactEntity>,
    onArtifactClick: (String, String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(artifacts) { artifact ->
            GlassCard(
                modifier = Modifier
                    .width(180.dp)
                    .height(260.dp)
                    .clickable { onArtifactClick(artifact.title, artifact.path) }
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text(
                            text = artifact.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SilverGlow,
                            maxLines = 4,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = artifact.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CelestialBlue.copy(alpha = 0.6f)
                        )
                    }
                    
                    if (artifact.progress >= 0f) {
                        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                            LinearProgressIndicator(
                                progress = artifact.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = CelestialBlue,
                                trackColor = SilverGlow.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}
