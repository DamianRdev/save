package com.damianrdev.save.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookmarkWithTagsAndCollection(
    @Embedded
    val bookmark: BookmarkEntity,

    @Relation(
        parentColumn = "collectionId",
        entityColumn = "id"
    )
    val collection: CollectionEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookmarkTagCrossRef::class,
            parentColumn = "bookmarkId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
