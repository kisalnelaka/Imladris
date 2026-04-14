package com.imladris.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.R
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.theme.*
import kotlinx.coroutines.flow.flowOf

@Composable
fun LibraryScreen(
    onArtifactClick: (String, String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(folders) { folder ->
                    LibraryFolderItem(folder) {
                        viewModel.navigateToFolder(folder.id)
                    }
                }
                items(artifacts) { artifact ->
                    LibraryArtifactItem(artifact) {
                        onArtifactClick(artifact.title, artifact.path)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryFolderItem(folder: FolderEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyLarge,
                color = SilverGlow
            )
        }
    }
}

@Composable
fun LibraryArtifactItem(artifact: ArtifactEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = SilverGlow.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = artifact.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SilverGlow
                )
                Text(
                    text = artifact.type.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverGlow.copy(alpha = 0.4f)
                )
            }
        }
    }
}
