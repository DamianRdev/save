package com.damianrdev.save.domain.repository

import com.damianrdev.save.domain.model.Bookmark
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.SearchFilter
import kotlinx.coroutines.flow.Flow

enum class ExportFormat {
    JSON, CSV, HTML
}

interface BookmarkRepository {
    fun getAllActiveBookmarks(): Flow<List<BookmarkWithDetails>>
    fun getBookmarkById(id: Long): Flow<BookmarkWithDetails?>
    fun getFavorites(): Flow<List<BookmarkWithDetails>>
    fun getArchived(): Flow<List<BookmarkWithDetails>>
    fun getTrash(): Flow<List<BookmarkWithDetails>>
    fun getByCollection(collectionId: Long): Flow<List<BookmarkWithDetails>>
    fun getUncategorized(): Flow<List<BookmarkWithDetails>>
    fun search(filter: SearchFilter): Flow<List<BookmarkWithDetails>>

    suspend fun saveBookmark(
        originalUrl: String,
        title: String? = null,
        note: String? = null,
        collectionId: Long? = null,
        tagNames: List<String> = emptyList()
    ): Long

    suspend fun updateBookmark(
        bookmark: Bookmark,
        tagNames: List<String>
    )

    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    suspend fun setRead(id: Long, isRead: Boolean)
    suspend fun moveToTrash(ids: List<Long>)
    suspend fun restoreFromTrash(ids: List<Long>)
    suspend fun deletePermanently(ids: List<Long>)
    suspend fun emptyTrash()
    suspend fun setArchived(ids: List<Long>, isArchived: Boolean)
    suspend fun moveToCollection(ids: List<Long>, collectionId: Long?)
    suspend fun refreshMetadata(bookmarkId: Long)

    suspend fun exportData(format: ExportFormat): String
    suspend fun importData(content: String, format: ExportFormat): Int
}
