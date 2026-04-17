package com.imladris.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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
import com.imladris.core.ui.theme.*
import kotlinx.coroutines.flow.flowOf

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentFolderId != null) {
                    IconButton(onClick = { viewModel.navigateToFolder(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = CelestialBlue)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (currentFolderId == null) stringResource(R.string.library_title) else "Sanctuary",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    color = CelestialBlue
                )
            }
            
            // View Mode Selector
            Row {
                IconButton(onClick = { viewMode = LibraryViewMode.LIST }) {
                    Icon(
                        imageVector = Icons.Default.List, 
                        contentDescription = "List View",
                        tint = if (viewMode == LibraryViewMode.LIST) CelestialBlue else SilverGlow.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = { viewMode = LibraryViewMode.COVERS }) {
                    Icon(
                        imageVector = Icons.Default.GridView, 
                        contentDescription = "Cover View",
                        tint = if (viewMode == LibraryViewMode.COVERS) CelestialBlue else SilverGlow.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        Text(
            text = stringResource(R.string.library_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = SilverGlow.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = if (currentFolderId == null) 0.dp else 48.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (folders.isEmpty() && artifacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_artifacts_found), color = SilverGlow.copy(alpha = 0.3f))
            }
        } else {
            when (viewMode) {
                LibraryViewMode.LIST -> LibraryListView(folders, artifacts, onArtifactClick, viewModel)
                LibraryViewMode.COVERS -> LibraryCoverView(folders, artifacts, onArtifactClick, viewModel)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = EtherealTeal.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = SilverGlow)
        }
    }
}

@Composable
fun LibraryFolderCoverItem(folder: FolderEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(0.7f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DeepMist.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, CelestialBlue.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = EtherealTeal.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = SilverGlow,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LibraryArtifactListItem(artifact: ArtifactEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(modifier = Modifier.size(40.dp, 60.dp)) {
                if (artifact.coverPath != null) {
                    AsyncImage(
                        model = artifact.coverPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                         Text(text = artifact.title.take(1), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = artifact.title, style = MaterialTheme.typography.bodyLarge, color = SilverGlow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = artifact.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = SilverGlow.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun LibraryArtifactCoverItem(artifact: ArtifactEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(0.7f).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DeepMist),
        shape = MaterialTheme.shapes.small
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
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Text(
                        text = artifact.title, 
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
