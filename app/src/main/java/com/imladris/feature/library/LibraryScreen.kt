package com.imladris.feature.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.imladris.R
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.components.MajesticBackground
import com.imladris.core.ui.theme.*
import kotlinx.coroutines.flow.flowOf
import kotlin.math.abs

enum class LibraryViewMode {
    LIST, COVERS
}

@Composable
fun LibraryScreen(
    onArtifactClick: (String, String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    var viewMode by remember { mutableStateOf(LibraryViewMode.COVERS) }

    val folders by remember(currentFolderId) {
        if (currentFolderId == null) viewModel.rootFolders 
        else viewModel.getFoldersIn(currentFolderId!!)
    }.collectAsState(initial = emptyList())

    val artifacts by remember(currentFolderId) {
        if (currentFolderId == null) flowOf(emptyList<ArtifactEntity>())
        else viewModel.getArtifactsIn(currentFolderId!!)
    }.collectAsState(initial = emptyList())

    MajesticBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentFolderId != null) {
                        IconButton(
                            onClick = { viewModel.navigateToFolder(null) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CelestialBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = if (currentFolderId == null) "Corridors" else "Sanctuary Archives",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SilverGlow
                        )
                        Text(
                            text = if (currentFolderId == null) "The great library of Rivendell" else "${artifacts.size} scrolls discovered",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = CelestialBlue.copy(alpha = 0.7f)
                        )
                    }
                }

                // View Mode Toggle Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        IconButton(
                            onClick = { viewMode = LibraryViewMode.COVERS },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (viewMode == LibraryViewMode.COVERS) CelestialBlue.copy(alpha = 0.2f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Cover View",
                                tint = if (viewMode == LibraryViewMode.COVERS) CelestialBlue else SilverGlow.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewMode = LibraryViewMode.LIST },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (viewMode == LibraryViewMode.LIST) CelestialBlue.copy(alpha = 0.2f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "List View",
                                tint = if (viewMode == LibraryViewMode.LIST) CelestialBlue else SilverGlow.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (folders.isEmpty() && artifacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CelestialBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, CelestialBlue.copy(alpha = 0.25f)),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = CelestialBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No Artifacts in this Corridor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SilverGlow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“May the wind under your wings bear you where the sun sails and the moon walks.”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 13.sp
                            ),
                            color = SilverGlow.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                when (viewMode) {
                    LibraryViewMode.LIST -> LibraryListView(folders, artifacts, onArtifactClick, viewModel)
                    LibraryViewMode.COVERS -> LibraryCoverView(folders, artifacts, onArtifactClick, viewModel)
                }
            }
        }
    }
}

@Composable
fun LibraryListView(
    folders: List<FolderEntity>,
    artifacts: List<ArtifactEntity>,
    onArtifactClick: (String, String) -> Unit,
    viewModel: LibraryViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(folders) { folder ->
            LibraryFolderListItem(folder) { viewModel.navigateToFolder(folder.id) }
        }
        items(artifacts) { artifact ->
            LibraryArtifactListItem(artifact) { onArtifactClick(artifact.title, artifact.path) }
        }
    }
}

@Composable
fun LibraryCoverView(
    folders: List<FolderEntity>,
    artifacts: List<ArtifactEntity>,
    onArtifactClick: (String, String) -> Unit,
    viewModel: LibraryViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(folders) { folder ->
            LibraryFolderCoverItem(folder) { viewModel.navigateToFolder(folder.id) }
        }
        items(artifacts) { artifact ->
            LibraryArtifactCoverItem(artifact) { onArtifactClick(artifact.title, artifact.path) }
        }
    }
}

@Composable
fun LibraryFolderListItem(folder: FolderEntity, onClick: () -> Unit) {
    GlassCard(
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EtherealTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = EtherealTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = folder.name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), color = SilverGlow)
                Text(text = "Gateway corridor", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SilverGlow.copy(alpha = 0.4f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SilverGlow.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun LibraryFolderCoverItem(folder: FolderEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF131822).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, EtherealTeal.copy(alpha = 0.25f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EtherealTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = EtherealTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = SilverGlow,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryArtifactListItem(artifact: ArtifactEntity, onClick: () -> Unit) {
    GlassCard(
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(10.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Spine thumbnail
            Box(
                modifier = Modifier
                    .size(38.dp, 52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(getSpineGradient(artifact.title)),
                contentAlignment = Alignment.Center
            ) {
                if (artifact.coverPath != null) {
                    AsyncImage(
                        model = artifact.coverPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = artifact.title.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artifact.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = SilverGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CelestialBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = artifact.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CelestialBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (artifact.progress > 0f) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(artifact.progress * 100).toInt()}% completed",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = EtherealTeal
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SilverGlow.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LibraryArtifactCoverItem(artifact: ArtifactEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF141923),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (artifact.coverPath != null) {
                AsyncImage(
                    model = artifact.coverPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Ornate Fantasy Book Cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(getSpineGradient(artifact.title))
                ) {
                    // Embossed Gold Border Inset
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .border(BorderStroke(0.75.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = artifact.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFD4AF37).copy(alpha = 0.7f)
                            )

                            Text(
                                text = artifact.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )

                            // Decorative Diamond
                            Text(
                                text = "❖",
                                color = Color(0xFFD4AF37).copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Bottom Progress Bar
            if (artifact.progress > 0f) {
                LinearProgressIndicator(
                    progress = artifact.progress,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp),
                    color = EtherealTeal,
                    trackColor = Color.Black.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun getSpineGradient(title: String): Brush {
    val hash = abs(title.hashCode())
    return when (hash % 4) {
        0 -> Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
        1 -> Brush.verticalGradient(listOf(Color(0xFF134E5E), Color(0xFF1E6B52), Color(0xFF2C5364)))
        2 -> Brush.verticalGradient(listOf(Color(0xFF2C1947), Color(0xFF4A1A4B), Color(0xFF241434)))
        else -> Brush.verticalGradient(listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF1E2022)))
    }
}
