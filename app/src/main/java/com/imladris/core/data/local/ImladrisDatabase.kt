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

    @Insert(onConflict = OnConflictAlignment.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictAlignment.REPLACE)
    suspend fun insertArtifact(artifact: ArtifactEntity)

    @Query("SELECT * FROM artifacts ORDER BY lastRead DESC LIMIT 5")
    fun getRecentArtifacts(): Flow<List<ArtifactEntity>>

    @Query("SELECT * FROM highlights WHERE artifactId = :artifactId")
    fun getHighlights(artifactId: String): Flow<List<HighlightEntity>>
}

@Database(
    entities = [FolderEntity::class, ArtifactEntity::class, HighlightEntity::class],
    version = 1
)
abstract class ImladrisDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
