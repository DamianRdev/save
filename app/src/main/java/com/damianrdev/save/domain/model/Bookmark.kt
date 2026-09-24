package com.damianrdev.save.domain.model

enum class SortOrder(val label: String) {
    NEWEST("Más recientes"),
    OLDEST("Más antiguos"),
    TITLE("Título (A-Z)"),
    LAST_OPENED("Última apertura"),
    DOMAIN("Fuente / Dominio"),
    READING_TIME("Tiempo de lectura")
}

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
    val contentType: String = "ARTICLE",
    val author: String? = null,
    val readingTimeMinutes: Int = 0,
    val readingProgress: Float = 0f,
    val lastOpenedAt: Long? = null,
    val readAt: Long? = null,
    val archivedAt: Long? = null,
    val offlineStatus: String = "NONE",
    val offlineHtmlContent: String? = null
)

data class Collection(
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val colorHex: String = "#6366F1",
    val iconName: String = "folder",
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
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
    val onlyRead: Boolean = false,
    val onlyOffline: Boolean = false,
    val contentType: String? = null,
    val collectionId: Long? = null,
    val tagId: Long? = null,
    val sourceDomain: String? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST
)
