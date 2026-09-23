package com.damianrdev.save.data.repository

import com.damianrdev.save.data.local.dao.TagDao
import com.damianrdev.save.data.local.entities.TagEntity
import com.damianrdev.save.domain.model.Tag
import com.damianrdev.save.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOrCreateTags(names: List<String>): List<Tag> {
        return names.mapNotNull { name ->
            val trimmed = name.trim()
            if (trimmed.isNotBlank()) {
                tagDao.getOrCreateTag(trimmed).toDomain()
            } else null
        }
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(TagEntity(id = tag.id, name = tag.name))
    }

    private fun TagEntity.toDomain(): Tag = Tag(id = id, name = name)
}
