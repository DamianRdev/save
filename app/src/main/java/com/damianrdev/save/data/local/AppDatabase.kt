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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        CollectionEntity::class,
        TagEntity::class,
        BookmarkTagCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "save_vault.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Non-destructive column additions to bookmarks
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN author TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN readingTimeMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN readingProgress REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN lastOpenedAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN readAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN archivedAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN offlineStatus TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN offlineHtmlContent TEXT DEFAULT NULL")

                // Indices for fast filtering
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_isRead ON bookmarks(isRead)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_contentType ON bookmarks(contentType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_offlineStatus ON bookmarks(offlineStatus)")

                // Non-destructive column additions to collections
                db.execSQL("ALTER TABLE collections ADD COLUMN description TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE collections ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE collections ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

                // Seed suggested default collections if not already present
                seedDefaultCollections(db)
            }
        }

        fun seedDefaultCollections(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            val defaults = listOf(
                Triple("Para leer", "#3B82F6", " Artículos y lecturas pendientes"),
                Triple("Universidad", "#8B5CF6", "Apuntes, papers y material de estudio"),
                Triple("Desarrollo", "#10B981", "Repositorios, documentación y código"),
                Triple("Inteligencia artificial", "#06B6D4", "Modelos, herramientas y noticias de IA"),
                Triple("Inspiración", "#EC4899", "Diseño, ideas y referencias visuales"),
                Triple("Personal", "#F59E0B", "Enlaces personales y utilidades")
            )
            defaults.forEachIndexed { index, (name, color, desc) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO collections (name, description, colorHex, iconName, position, createdAt, updatedAt) VALUES (?, ?, ?, 'folder', ?, ?, ?)",
                    arrayOf<Any>(name, desc.trim(), color, index, now, now)
                )
            }
        }
    }
}
