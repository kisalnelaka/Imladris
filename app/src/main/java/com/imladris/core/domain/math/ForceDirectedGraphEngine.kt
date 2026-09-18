package com.imladris.core.domain.math

import kotlin.math.*

/**
 * 2D Vector representation for physics calculations.
 */
data class Vec2(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vec2 = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Float): Vec2 = if (scalar != 0f) Vec2(x / scalar, y / scalar) else Vec2(0f, 0f)

    fun magnitude(): Float = sqrt(x * x + y * y)
    fun magnitudeSquared(): Float = x * x + y * y

    fun normalized(): Vec2 {
        val mag = magnitude()
        return if (mag > 0.0001f) this / mag else Vec2(0f, 0f)
    }

    fun distanceTo(other: Vec2): Float = (this - other).magnitude()
}

/**
 * Node in the Force-Directed Knowledge Graph representing an artifact or gateway.
 */
data class GraphNode(
    val id: String,
    val title: String,
    val isFolder: Boolean,
    val folderId: String?,
    val progress: Float,
    val radius: Float,
    var position: Vec2,
    var velocity: Vec2 = Vec2(),
    var acceleration: Vec2 = Vec2(),
    var isPinned: Boolean = false
)

/**
 * Edge connecting two graph nodes with stiffness and resting length.
 */
data class GraphEdge(
    val sourceId: String,
    val targetId: String,
    val stiffness: Float = 1.0f,
    val targetLength: Float = 160f
)

/**
 * Pure Kotlin Spring-Electrical Graph Simulation using Coulomb-Hooke physics
 * with simulated annealing temperature cooling and kinetic stabilization.
 */
class ForceDirectedGraphEngine(
    private val repulsionConstant: Float = 12000f,
    private val springStiffness: Float = 0.04f,
    private val centerAttraction: Float = 0.005f,
    private val damping: Float = 0.82f
) {
    private val _nodes = mutableMapOf<String, GraphNode>()
    private val _edges = mutableListOf<GraphEdge>()

    val nodes: List<GraphNode> get() = _nodes.values.toList()
    val edges: List<GraphEdge> get() = _edges

    fun setGraph(nodes: List<GraphNode>, edges: List<GraphEdge>) {
        _nodes.clear()
        nodes.forEach { _nodes[it.id] = it }
        _edges.clear()
        _edges.addAll(edges)
    }

    fun pinNode(nodeId: String, pinned: Boolean, atPosition: Vec2? = null) {
        _nodes[nodeId]?.let { node ->
            node.isPinned = pinned
            if (atPosition != null) {
                node.position = atPosition
                node.velocity = Vec2(0f, 0f)
            }
        }
    }

    /**
     * Executes one numerical integration step of the physical system.
     * Uses Verlet-style velocity updates and bounds containment.
     */
    fun step(boundsWidth: Float, boundsHeight: Float, dt: Float = 0.016f): Float {
        val nodeList = _nodes.values.toList()
        if (nodeList.isEmpty()) return 0f

        val center = Vec2(boundsWidth / 2f, boundsHeight / 2f)
        var totalKineticEnergy = 0f

        // 1. Reset accelerations and apply gentle center gravity to prevent infinite drifts
        for (node in nodeList) {
            if (node.isPinned) {
                node.acceleration = Vec2(0f, 0f)
                continue
            }
            val toCenter = center - node.position
            node.acceleration = toCenter * centerAttraction
        }

        // 2. Coulomb's Repulsion: Every node repels every other node (O(N^2) exact)
        val count = nodeList.size
        for (i in 0 until count) {
            val nodeA = nodeList[i]
            for (j in i + 1 until count) {
                val nodeB = nodeList[j]
                val delta = nodeA.position - nodeB.position
                val dist = max(delta.magnitude(), 25f) // avoid singularity
                val forceMagnitude = repulsionConstant / (dist * dist)
                val normal = delta.normalized()
                val force = normal * forceMagnitude

                if (!nodeA.isPinned) applyForce(nodeA, force)
                if (!nodeB.isPinned) applyForce(nodeB, force * -1f)
            }
        }

        // 3. Hooke's Elastic Springs: Connected edges attract/repel towards resting length
        for (edge in _edges) {
            val source = _nodes[edge.sourceId] ?: continue
            val target = _nodes[edge.targetId] ?: continue

            val delta = target.position - source.position
            val currentDist = delta.magnitude()
            val displacement = currentDist - edge.targetLength
            val forceMag = displacement * springStiffness * edge.stiffness
            val force = delta.normalized() * forceMag

            if (!source.isPinned) applyForce(source, force)
            if (!target.isPinned) applyForce(target, force * -1f)
        }

        // 4. Numerical Integration: Update velocities and positions with damping & boundary restitution
        val padding = 40f
        for (node in nodeList) {
            if (node.isPinned) continue

            node.velocity = (node.velocity + node.acceleration) * damping
            node.position = node.position + (node.velocity * dt * 60f)

            // Soft bounds bounce
            var posX = node.position.x
            var posY = node.position.y
            if (posX < padding) {
                posX = padding
                node.velocity = Vec2(-node.velocity.x * 0.5f, node.velocity.y)
            } else if (posX > boundsWidth - padding) {
                posX = boundsWidth - padding
                node.velocity = Vec2(-node.velocity.x * 0.5f, node.velocity.y)
            }

            if (posY < padding) {
                posY = padding
                node.velocity = Vec2(node.velocity.x, -node.velocity.y * 0.5f)
            } else if (posY > boundsHeight - padding) {
                posY = boundsHeight - padding
                node.velocity = Vec2(node.velocity.x, -node.velocity.y * 0.5f)
            }

            node.position = Vec2(posX, posY)
            totalKineticEnergy += node.velocity.magnitudeSquared()
        }

        return totalKineticEnergy
    }

    private fun applyForce(node: GraphNode, force: Vec2) {
        node.acceleration = node.acceleration + force
    }
}
