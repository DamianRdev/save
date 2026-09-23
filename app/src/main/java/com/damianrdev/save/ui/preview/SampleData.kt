package com.damianrdev.save.ui.preview

import com.damianrdev.save.domain.model.Bookmark
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.model.Tag

object SampleData {

    val sampleCollection1 = Collection(
        id = 1L,
        name = "Tecnología",
        colorHex = "#6366F1",
        iconName = "folder",
        bookmarkCount = 12
    )

    val sampleCollection2 = Collection(
        id = 2L,
        name = "Recetas & Cocina",
        colorHex = "#10B981",
        iconName = "folder",
        bookmarkCount = 5
    )

    val sampleTags = listOf(
        Tag(id = 1L, name = "android"),
        Tag(id = 2L, name = "kotlin"),
        Tag(id = 3L, name = "compose"),
        Tag(id = 4L, name = "s24ultra")
    )

    val sampleBookmark1 = BookmarkWithDetails(
        bookmark = Bookmark(
            id = 1L,
            originalUrl = "https://developer.android.com/jetpack/compose",
            normalizedUrl = "https://developer.android.com/jetpack/compose",
            title = "Jetpack Compose: Toolkit moderno para UI nativa de Android",
            description = "Jetpack Compose es el kit de herramientas moderno de Android para compilar interfaces de usuario nativas de forma declarativa.",
            thumbnailUrl = "https://developer.android.com/images/social/android-developers.png",
            sourceDomain = "developer.android.com",
            sourceApp = "Chrome",
            note = "Revisar arquitectura de estados y composición recomposition triggers",
            isFavorite = true,
            isArchived = false,
            isRead = false,
            createdAt = System.currentTimeMillis() - 3600000L,
            collectionId = 1L,
            contentType = "ARTICLE"
        ),
        collection = sampleCollection1,
        tags = listOf(sampleTags[0], sampleTags[1], sampleTags[2])
    )

    val sampleBookmark2 = BookmarkWithDetails(
        bookmark = Bookmark(
            id = 2L,
            originalUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            normalizedUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            title = "Guía completa de optimización AMOLED para Samsung Galaxy S24 Ultra",
            description = "Aprende cómo exprimir la pantalla Dynamic AMOLED 2X a 120Hz con perfiles de color y negros absolutos.",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            sourceDomain = "youtube.com",
            sourceApp = "YouTube",
            note = "Muy buenos tips sobre refresco variable y batería",
            isFavorite = false,
            isArchived = false,
            isRead = true,
            createdAt = System.currentTimeMillis() - 86400000L,
            collectionId = 1L,
            contentType = "VIDEO"
        ),
        collection = sampleCollection1,
        tags = listOf(sampleTags[0], sampleTags[3])
    )

    val sampleBookmarksList = listOf(sampleBookmark1, sampleBookmark2)
}
