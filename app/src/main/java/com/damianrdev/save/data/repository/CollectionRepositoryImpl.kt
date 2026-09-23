package com.damianrdev.save.data.repository

import com.damianrdev.save.data.local.dao.CollectionDao
import com.damianrdev.save.data.local.entities.CollectionEntity
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao
) : CollectionRepository {

    override fun getAllCollections(): Flow<List<Collection>> {
        return collectionDao.getCollectionsWithCount().map { list ->
            list.map { item ->
                Collection(
                    id = item.id,
                    name = item.name,
                    colorHex = item.colorHex,
                    iconName = item.iconName,
                    createdAt = item.createdAt,
                    bookmarkCount = item.bookmarkCount
                )
            }
        }
    }

    override suspend fun createCollection(name: String, colorHex: String, iconName: String): Long {
        return collectionDao.insertCollection(
            CollectionEntity(
                name = name.trim(),
                colorHex = colorHex,
                iconName = iconName
            )
        )
    }

    override suspend fun updateCollection(collection: Collection) {
        collectionDao.updateCollection(
            CollectionEntity(
                id = collection.id,
                name = collection.name.trim(),
                colorHex = collection.colorHex,
                iconName = collection.iconName,
                createdAt = collection.createdAt
            )
        )
    }

    override suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(
            CollectionEntity(
                id = collection.id,
                name = collection.name
            )
        )
    }
}
