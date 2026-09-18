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
    val parentFolderId: String?,
    val lastPage: Int = 0,
    val totalPages: Int = 1,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 0,
    val readingSpeedWpm: Float = 220f
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artifactId: String,
    val content: String,
    val page: Int,
    val timestamp: Long,
    val color: Int,
    val note: String? = null
)

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ArtifactEntity::class,
            parentColumns = ["id"],
            childColumns = ["artifactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["artifactId"])]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artifactId: String,
    val startTime: Long,
    val durationMs: Long,
    val wordsRead: Int,
    val progressDelta: Float
)
