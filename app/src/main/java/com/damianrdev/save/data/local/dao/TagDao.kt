package com.damianrdev.save.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.damianrdev.save.data.local.entities.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Transaction
    suspend fun getOrCreateTag(name: String): TagEntity {
        val trimmed = name.trim().lowercase()
        val existing = getTagByName(trimmed)
        if (existing != null) return existing
        val id = insertTag(TagEntity(name = trimmed))
        return TagEntity(id = id, name = trimmed)
    }
}
