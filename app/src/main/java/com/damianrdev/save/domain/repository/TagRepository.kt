package com.damianrdev.save.domain.repository

import com.damianrdev.save.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getOrCreateTags(names: List<String>): List<Tag>
    suspend fun deleteTag(tag: Tag)
}
