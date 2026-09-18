package com.imladris.core.data.local

import androidx.room.*
import com.imladris.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM folders WHERE parentId IS NULL")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId")
    fun getFoldersIn(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM artifacts WHERE parentFolderId = :folderId")
    fun getArtifactsIn(folderId: String): Flow<List<ArtifactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifact(artifact: ArtifactEntity)

    @Query("UPDATE artifacts SET lastPage = :lastPage, totalPages = :totalPages, progress = :progress, lastRead = :lastRead, readingTimeMinutes = readingTimeMinutes + :addedMinutes, readingSpeedWpm = :wpm WHERE id = :id")
    suspend fun updateArtifactReadingState(
        id: String,
        lastPage: Int,
        totalPages: Int,
        progress: Float,
        lastRead: Long,
        addedMinutes: Int,
        wpm: Float
    )

    @Query("SELECT * FROM artifacts ORDER BY lastRead DESC LIMIT 10")
    fun getRecentlyOpened(): Flow<List<ArtifactEntity>>
    
    // Alias for compatibility with other modules
    @Query("SELECT * FROM artifacts ORDER BY lastRead DESC LIMIT 10")
    fun getRecentArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts ORDER BY addedDate DESC LIMIT 10")
    fun getRecentlyAdded(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts WHERE progress > 0.05 AND progress < 0.98 ORDER BY lastRead DESC")
    fun getArtifactsInProgress(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts WHERE progress = 0 ORDER BY addedDate DESC")
    fun getUnreadArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT COUNT(*) FROM artifacts")
    fun getArtifactCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM highlights")
    fun getHighlightCount(): Flow<Int>

    @Query("SELECT * FROM highlights WHERE artifactId = :artifactId ORDER BY page ASC, timestamp ASC")
    fun getHighlights(artifactId: String): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteHighlight(id: Long)
    
    @Query("SELECT * FROM artifacts")
    fun getAllArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts WHERE id = :id")
    suspend fun getArtifactById(id: String): ArtifactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingSession(session: ReadingSessionEntity): Long

    @Query("SELECT * FROM reading_sessions ORDER BY startTime DESC")
    fun getAllReadingSessions(): Flow<List<ReadingSessionEntity>>

    @Query("SELECT SUM(durationMs) FROM reading_sessions")
    fun getTotalReadingDurationMs(): Flow<Long?>

    @Query("SELECT SUM(wordsRead) FROM reading_sessions")
    fun getTotalWordsRead(): Flow<Int?>

    @Query("DELETE FROM artifacts")
    suspend fun deleteAllArtifacts()

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}

@Database(
    entities = [FolderEntity::class, ArtifactEntity::class, HighlightEntity::class, ReadingSessionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ImladrisDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
