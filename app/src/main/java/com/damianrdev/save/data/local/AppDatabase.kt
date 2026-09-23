package com.damianrdev.save.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.damianrdev.save.data.local.dao.BookmarkDao
import com.damianrdev.save.data.local.dao.CollectionDao
import com.damianrdev.save.data.local.dao.TagDao
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkTagCrossRef
import com.damianrdev.save.data.local.entities.CollectionEntity
import com.damianrdev.save.data.local.entities.TagEntity

@Database(
    entities = [
        BookmarkEntity::class,
        CollectionEntity::class,
        TagEntity::class,
        BookmarkTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "save_vault.db"
    }
}
