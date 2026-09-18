package com.imladris.feature.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imladris.core.data.local.entities.ArtifactEntity
import com.imladris.core.data.local.entities.FolderEntity
import com.imladris.core.data.repository.LibraryRepository
import com.imladris.core.domain.math.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class KnowledgeGraphViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    private val physicsEngine = ForceDirectedGraphEngine()

    private val _nodes = MutableStateFlow<List<GraphNode>>(emptyList())
    val nodes: StateFlow<List<GraphNode>> = _nodes.asStateFlow()

    private val _edges = MutableStateFlow<List<GraphEdge>>(emptyList())
    val edges: StateFlow<List<GraphEdge>> = _edges.asStateFlow()

    private val _selectedNode = MutableStateFlow<GraphNode?>(null)
    val selectedNode: StateFlow<GraphNode?> = _selectedNode.asStateFlow()

    private val _associatedArtifact = MutableStateFlow<ArtifactEntity?>(null)
    val associatedArtifact: StateFlow<ArtifactEntity?> = _associatedArtifact.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllArtifacts(),
                repository.getAllFolders()
            ) { artifacts, folders ->
                buildGraph(artifacts, folders)
            }.collect { (newNodes, newEdges) ->
                physicsEngine.setGraph(newNodes, newEdges)
                _nodes.value = physicsEngine.nodes
                _edges.value = physicsEngine.edges
            }
        }
    }

    private fun buildGraph(
        artifacts: List<ArtifactEntity>,
        folders: List<FolderEntity>
    ): Pair<List<GraphNode>, List<GraphEdge>> {
        val graphNodes = mutableListOf<GraphNode>()
        val graphEdges = mutableListOf<GraphEdge>()

        // Gateway folder nodes
        folders.forEach { folder ->
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val radius = 200f + Random.nextFloat() * 150f
            val posX = 500f + kotlin.math.cos(angle) * radius
            val posY = 500f + kotlin.math.sin(angle) * radius

            graphNodes.add(
                GraphNode(
                    id = "folder_${folder.id}",
                    title = folder.name,
                    isFolder = true,
                    folderId = folder.id,
                    progress = 1.0f,
                    radius = 28f,
                    position = Vec2(posX, posY)
                )
            )
        }

        // Artifact book nodes
        artifacts.forEach { artifact ->
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val radius = 100f + Random.nextFloat() * 250f
            val posX = 500f + kotlin.math.cos(angle) * radius
            val posY = 500f + kotlin.math.sin(angle) * radius

            val nodeRadius = 14f + (artifact.progress * 16f)

            graphNodes.add(
                GraphNode(
                    id = artifact.id,
                    title = artifact.title,
                    isFolder = false,
                    folderId = artifact.parentFolderId,
                    progress = artifact.progress,
                    radius = nodeRadius,
                    position = Vec2(posX, posY)
                )
            )

            // Connect artifact to parent folder gateway
            if (artifact.parentFolderId != null) {
                graphEdges.add(
                    GraphEdge(
                        sourceId = "folder_${artifact.parentFolderId}",
                        targetId = artifact.id,
                        stiffness = 1.2f,
                        targetLength = 120f
                    )
                )
            }
        }

        // Link neighboring artifacts in the same folder
        val grouped = artifacts.groupBy { it.parentFolderId }
        grouped.values.forEach { group ->
            for (i in 0 until group.size - 1) {
                graphEdges.add(
                    GraphEdge(
                        sourceId = group[i].id,
                        targetId = group[i + 1].id,
                        stiffness = 0.6f,
                        targetLength = 100f
                    )
                )
            }
        }

        return Pair(graphNodes, graphEdges)
    }

    fun stepPhysics(width: Float, height: Float) {
        if (_nodes.value.isNotEmpty()) {
            physicsEngine.step(width, height)
            _nodes.value = physicsEngine.nodes
        }
    }

    fun onNodeDrag(nodeId: String, newPos: Vec2) {
        physicsEngine.pinNode(nodeId, pinned = true, atPosition = newPos)
        _nodes.value = physicsEngine.nodes
    }

    fun onNodeRelease(nodeId: String) {
        physicsEngine.pinNode(nodeId, pinned = false)
    }

    fun selectNode(node: GraphNode?) {
        _selectedNode.value = node
        if (node != null && !node.isFolder) {
            viewModelScope.launch {
                _associatedArtifact.value = repository.getArtifactById(node.id)
            }
        } else {
            _associatedArtifact.value = null
        }
    }
}
