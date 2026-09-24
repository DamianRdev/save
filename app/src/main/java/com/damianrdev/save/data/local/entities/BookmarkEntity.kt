package com.damianrdev.save.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["normalizedUrl"]),
        Index(value = ["collectionId"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isArchived"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isRead"]),
        Index(value = ["contentType"]),
        Index(value = ["offlineStatus"]),
        Index(value = ["createdAt"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
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
    val metadataStatus: String = "PENDING", // PENDING, SUCCESS, FAILED, OFFLINE
    val contentType: String = "ARTICLE",    // ARTICLE, VIDEO, IMAGE, TWEET, CODE, PRODUCT, AUDIO, PDF, OTHER
    val author: String? = null,
    val readingTimeMinutes: Int = 0,
    val readingProgress: Float = 0f,
    val lastOpenedAt: Long? = null,
    val readAt: Long? = null,
    val archivedAt: Long? = null,
    val offlineStatus: String = "NONE",     // NONE, DOWNLOADING, AVAILABLE, FAILED
    val offlineHtmlContent: String? = null
)
