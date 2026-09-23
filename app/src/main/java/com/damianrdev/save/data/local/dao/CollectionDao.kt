package com.damianrdev.save.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.damianrdev.save.data.local.entities.CollectionEntity
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val id: Long,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val createdAt: Long,
    val bookmarkCount: Int
)

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollections(collections: List<CollectionEntity>): List<Long>

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections ORDER BY name ASC")
    suspend fun getAllCollectionsSync(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    fun getCollectionById(id: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE name = :name LIMIT 1")
    suspend fun getCollectionByName(name: String): CollectionEntity?

    @Query("""
        SELECT c.id, c.name, c.colorHex, c.iconName, c.createdAt, 
               COUNT(b.id) AS bookmarkCount
        FROM collections c
        LEFT JOIN bookmarks b ON c.id = b.collectionId AND b.isDeleted = 0 AND b.isArchived = 0
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getCollectionsWithCount(): Flow<List<CollectionWithCount>>
}
