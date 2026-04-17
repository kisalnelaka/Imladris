package com.imladris.core.data.local

import androidx.room.*
import com.imladris.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM folders WHERE parentId IS NULL")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId")
    fun getFoldersIn(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM artifacts WHERE parentFolderId = :folderId")
    fun getArtifactsIn(folderId: String): Flow<List<ArtifactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifact(artifact: ArtifactEntity)

    @Query("SELECT * FROM artifacts ORDER BY lastRead DESC LIMIT 10")
    fun getRecentlyOpened(): Flow<List<ArtifactEntity>>
    
    // Alias for compatibility with other modules
    @Query("SELECT * FROM artifacts ORDER BY lastRead DESC LIMIT 10")
    fun getRecentArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts ORDER BY addedDate DESC LIMIT 10")
    fun getRecentlyAdded(): Flow<List<ArtifactEntity>>

    @Query("SELECT COUNT(*) FROM artifacts")
    fun getArtifactCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM highlights")
    fun getHighlightCount(): Flow<Int>

    @Query("SELECT * FROM highlights WHERE artifactId = :artifactId")
    fun getHighlights(artifactId: String): Flow<List<HighlightEntity>>
    
    @Query("SELECT * FROM artifacts")
    fun getAllArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM artifacts WHERE id = :id")
    suspend fun getArtifactById(id: String): ArtifactEntity?

    @Query("DELETE FROM artifacts")
    suspend fun deleteAllArtifacts()

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}

@Database(
    entities = [FolderEntity::class, ArtifactEntity::class, HighlightEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ImladrisDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
