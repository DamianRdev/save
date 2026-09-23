package com.damianrdev.save.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkTagCrossRef
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>): List<Long>

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    fun getBookmarkById(id: Long): Flow<BookmarkWithTagsAndCollection?>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkByIdSync(id: Long): BookmarkWithTagsAndCollection?

    @Query("SELECT * FROM bookmarks WHERE normalizedUrl = :normalizedUrl AND isDeleted = 0 LIMIT 1")
    suspend fun getBookmarkByNormalizedUrl(normalizedUrl: String): BookmarkEntity?

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveBookmarks(): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getAllActiveBookmarksSync(): List<BookmarkWithTagsAndCollection>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isArchived = 0 AND collectionId = :collectionId ORDER BY createdAt DESC")
    fun getBookmarksByCollection(collectionId: Long): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isArchived = 0 AND collectionId IS NULL ORDER BY createdAt DESC")
    fun getUncategorizedBookmarks(): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteBookmarks(): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 0 AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedBookmarks(): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getTrashBookmarks(): Flow<List<BookmarkWithTagsAndCollection>>

    @Transaction
    @Query("""
        SELECT DISTINCT b.* FROM bookmarks b
        LEFT JOIN bookmark_tag_cross_ref bt ON b.id = bt.bookmarkId
        LEFT JOIN tags t ON bt.tagId = t.id
        LEFT JOIN collections c ON b.collectionId = c.id
        WHERE b.isDeleted = 0
          AND (
            b.title LIKE '%' || :query || '%'
            OR b.description LIKE '%' || :query || '%'
            OR b.note LIKE '%' || :query || '%'
            OR b.originalUrl LIKE '%' || :query || '%'
            OR b.sourceDomain LIKE '%' || :query || '%'
            OR t.name LIKE '%' || :query || '%'
            OR c.name LIKE '%' || :query || '%'
          )
        ORDER BY b.createdAt DESC
    """)
    fun searchBookmarks(query: String): Flow<List<BookmarkWithTagsAndCollection>>

    // Batch & State Modifiers
    @Query("UPDATE bookmarks SET isDeleted = 1, updatedAt = :timestamp WHERE id IN (:ids)")
    suspend fun moveToTrash(ids: List<Long>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bookmarks SET isDeleted = 0, updatedAt = :timestamp WHERE id IN (:ids)")
    suspend fun restoreFromTrash(ids: List<Long>, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM bookmarks WHERE id IN (:ids)")
    suspend fun deletePermanently(ids: List<Long>)

    @Query("DELETE FROM bookmarks WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE bookmarks SET isArchived = :isArchived, updatedAt = :timestamp WHERE id IN (:ids)")
    suspend fun setArchived(ids: List<Long>, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bookmarks SET collectionId = :collectionId, updatedAt = :timestamp WHERE id IN (:ids)")
    suspend fun moveToCollection(ids: List<Long>, collectionId: Long?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bookmarks SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bookmarks SET isRead = :isRead, updatedAt = :timestamp WHERE id = :id")
    suspend fun setRead(id: Long, isRead: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE bookmarks SET metadataStatus = :status, title = :title, description = :description, thumbnailUrl = :thumbnailUrl, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateMetadata(
        id: Long,
        status: String,
        title: String,
        description: String?,
        thumbnailUrl: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    // Tag Relationships
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: BookmarkTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<BookmarkTagCrossRef>)

    @Query("DELETE FROM bookmark_tag_cross_ref WHERE bookmarkId = :bookmarkId")
    suspend fun deleteCrossRefsForBookmark(bookmarkId: Long)

    @Transaction
    suspend fun updateBookmarkTags(bookmarkId: Long, tagIds: List<Long>) {
        deleteCrossRefsForBookmark(bookmarkId)
        val crossRefs = tagIds.map { tagId -> BookmarkTagCrossRef(bookmarkId, tagId) }
        insertCrossRefs(crossRefs)
    }
}
