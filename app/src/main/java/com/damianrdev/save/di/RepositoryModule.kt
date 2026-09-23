package com.damianrdev.save.di

import com.damianrdev.save.data.remote.JsoupMetadataExtractor
import com.damianrdev.save.data.remote.MetadataExtractor
import com.damianrdev.save.data.repository.BookmarkRepositoryImpl
import com.damianrdev.save.data.repository.CollectionRepositoryImpl
import com.damianrdev.save.data.repository.TagRepositoryImpl
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.CollectionRepository
import com.damianrdev.save.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        impl: BookmarkRepositoryImpl
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(
        impl: CollectionRepositoryImpl
    ): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindMetadataExtractor(
        impl: JsoupMetadataExtractor
    ): MetadataExtractor
}
