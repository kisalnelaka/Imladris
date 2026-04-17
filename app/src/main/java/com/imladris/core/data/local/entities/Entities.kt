package com.imladris.core.data.local.entities

import androidx.room.*

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val parentId: String?,
    val glowColor: Int
)

@Entity(
    tableName = "artifacts",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentFolderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentFolderId"])]
)
data class ArtifactEntity(
    @PrimaryKey val id: String,
    val title: String,
    val path: String,
    val type: String,
    val coverPath: String?,
    val lastRead: Long,
    val addedDate: Long,
    val progress: Float,
    val parentFolderId: String?
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artifactId: String,
    val content: String,
    val page: Int,
    val timestamp: Long,
    val color: Int
)
