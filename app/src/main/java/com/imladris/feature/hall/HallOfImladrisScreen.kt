package com.imladris.feature.hall

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.ui.components.ElvenDivider
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.components.MajesticBackground
import com.imladris.core.ui.theme.*
import com.imladris.feature.library.LibraryViewModel

@Composable
fun HallOfImladrisScreen(
    onArtifactClick: (String, String) -> Unit,
    onSettingsClick: () -> Unit,
    hallViewModel: HallViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val recentlyOpened by hallViewModel.recentlyOpened.collectAsState(initial = emptyList())
    val recentlyAdded by hallViewModel.recentlyAdded.collectAsState(initial = emptyList())
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { libraryViewModel.scanDirectory(it) }
    }

    MajesticBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Library",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            fontSize = 36.sp
                        ),
                        color = SilverGlow
                    )
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SilverGlow)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (recentlyOpened.isNotEmpty()) {
                    LibrarySectionHeader("Continue Reading")
                    Spacer(modifier = Modifier.height(16.dp))
                    ArtifactRow(recentlyOpened, onArtifactClick)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                if (recentlyAdded.isNotEmpty()) {
                    LibrarySectionHeader("Recently Added")
                    Spacer(modifier = Modifier.height(16.dp))
                    ArtifactRow(recentlyAdded, onArtifactClick)
                }

                if (recentlyOpened.isEmpty() && recentlyAdded.isEmpty()) {
                    Spacer(modifier = Modifier.height(100.dp))
                    EmptyLibraryPrompt { launcher.launch(null) }
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
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import Folder", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun LibrarySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        color = Champagne
    )
}

@Composable
fun ArtifactRow(
    artifacts: List<ArtifactEntity>,
    onArtifactClick: (String, String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(artifacts) { artifact ->
            GlassCard(
                modifier = Modifier
                    .width(160.dp)
                    .height(230.dp)
                    .clickable { onArtifactClick(artifact.title, artifact.path) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (artifact.coverPath != null) {
                        AsyncImage(
                            model = artifact.coverPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.6f
                        )
                    } else {
                        // Majestic text placeholder
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = artifact.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 4
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                    ) {
                        Text(
                            text = artifact.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = SilverGlow,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                        if (artifact.progress > 0f) {
                            LinearProgressIndicator(
                                progress = artifact.progress,
                                modifier = Modifier.fillMaxWidth().height(2.dp).padding(top = 4.dp),
                                color = CelestialBlue,
                                trackColor = SilverGlow.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryPrompt(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your library is empty.",
            style = MaterialTheme.typography.headlineSmall,
            color = SilverGlow.copy(alpha = 0.5f),
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = CelestialBlue.copy(alpha = 0.2f))
        ) {
            Text("Import Books", color = CelestialBlue)
        }
    }
}
