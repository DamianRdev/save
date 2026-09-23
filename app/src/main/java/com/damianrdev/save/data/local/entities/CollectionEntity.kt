package com.damianrdev.save.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collections",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorHex: String = "#6366F1",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
)
