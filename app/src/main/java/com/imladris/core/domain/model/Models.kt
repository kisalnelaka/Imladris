package com.imladris.core.domain.model

data class Artifact(
    val id: String,
    val title: String,
    val path: String,
    val type: ArtifactType,
    val progress: Float,
    val lastRead: Long
)

enum class ArtifactType {
    PDF, EPUB, MARKDOWN
}

data class Folder(
    val id: String,
    val name: String,
    val path: String,
    val subfolders: List<Folder> = emptyList(),
    val artifacts: List<Artifact> = emptyList()
)
