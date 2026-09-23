package com.damianrdev.save.domain.model

data class Bookmark(
    val id: Long = 0L,
    val originalUrl: String,
    val normalizedUrl: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val sourceDomain: String,
    val sourceApp: String? = null,
    val note: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val collectionId: Long? = null,
    val metadataStatus: String = "PENDING",
    val contentType: String = "ARTICLE"
)

data class Collection(
    val id: Long = 0L,
    val name: String,
    val colorHex: String = "#6366F1",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis(),
    val bookmarkCount: Int = 0
)

data class Tag(
    val id: Long = 0L,
    val name: String
)

data class BookmarkWithDetails(
    val bookmark: Bookmark,
    val collection: Collection?,
    val tags: List<Tag>
)

data class SearchFilter(
    val query: String = "",
    val onlyFavorites: Boolean = false,
    val onlyArchived: Boolean = false,
    val onlyUnread: Boolean = false,
    val collectionId: Long? = null,
    val tagId: Long? = null,
    val sourceDomain: String? = null
)
