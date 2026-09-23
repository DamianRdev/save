package com.damianrdev.save.domain.repository

import com.damianrdev.save.domain.model.Collection
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getAllCollections(): Flow<List<Collection>>
    suspend fun createCollection(name: String, colorHex: String = "#6366F1", iconName: String = "folder"): Long
    suspend fun updateCollection(collection: Collection)
    suspend fun deleteCollection(collection: Collection)
}
