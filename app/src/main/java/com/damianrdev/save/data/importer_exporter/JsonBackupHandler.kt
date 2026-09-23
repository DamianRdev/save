package com.damianrdev.save.data.importer_exporter

import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import com.damianrdev.save.data.local.entities.CollectionEntity
import com.damianrdev.save.data.local.entities.TagEntity
import org.json.JSONArray
import org.json.JSONObject

data class ImportedData(
    val collections: List<CollectionEntity>,
    val tags: List<TagEntity>,
    val bookmarksWithDetails: List<ImportedBookmark>
)

data class ImportedBookmark(
    val bookmark: BookmarkEntity,
    val collectionName: String?,
    val tagNames: List<String>
)

object JsonBackupHandler {

    private const val SCHEMA_VERSION = 1

    fun exportToJson(
        bookmarks: List<BookmarkWithTagsAndCollection>,
        collections: List<CollectionEntity>
    ): String {
        val root = JSONObject()
        root.put("version", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "Save")

        // Collections
        val collectionsArray = JSONArray()
        collections.forEach { col ->
            val colObj = JSONObject()
            colObj.put("id", col.id)
            colObj.put("name", col.name)
            colObj.put("colorHex", col.colorHex)
            colObj.put("iconName", col.iconName)
            colObj.put("createdAt", col.createdAt)
            collectionsArray.put(colObj)
        }
        root.put("collections", collectionsArray)

        // Bookmarks
        val bookmarksArray = JSONArray()
        bookmarks.forEach { item ->
            val b = item.bookmark
            val bObj = JSONObject()
            bObj.put("originalUrl", b.originalUrl)
            bObj.put("normalizedUrl", b.normalizedUrl)
            bObj.put("title", b.title)
            bObj.put("description", b.description ?: "")
            bObj.put("thumbnailUrl", b.thumbnailUrl ?: "")
            bObj.put("sourceDomain", b.sourceDomain)
            bObj.put("note", b.note ?: "")
            bObj.put("isFavorite", b.isFavorite)
            bObj.put("isArchived", b.isArchived)
            bObj.put("isRead", b.isRead)
            bObj.put("createdAt", b.createdAt)
            bObj.put("contentType", b.contentType)
            bObj.put("collectionName", item.collection?.name ?: "")

            val tagsArray = JSONArray()
            item.tags.forEach { tag ->
                tagsArray.put(tag.name)
            }
            bObj.put("tags", tagsArray)
            bookmarksArray.put(bObj)
        }
        root.put("bookmarks", bookmarksArray)

        return root.toString(2)
    }

    fun importFromJson(jsonString: String): ImportedData {
        val root = JSONObject(jsonString)
        val collectionsList = mutableListOf<CollectionEntity>()
        val tagsMap = mutableMapOf<String, TagEntity>()
        val importedBookmarks = mutableListOf<ImportedBookmark>()

        // Parse collections
        if (root.has("collections")) {
            val collectionsArray = root.getJSONArray("collections")
            for (i in 0 until collectionsArray.length()) {
                val colObj = collectionsArray.getJSONObject(i)
                collectionsList.add(
                    CollectionEntity(
                        name = colObj.getString("name"),
                        colorHex = colObj.optString("colorHex", "#6366F1"),
                        iconName = colObj.optString("iconName", "folder"),
                        createdAt = colObj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Parse bookmarks
        if (root.has("bookmarks")) {
            val bookmarksArray = root.getJSONArray("bookmarks")
            for (i in 0 until bookmarksArray.length()) {
                val bObj = bookmarksArray.getJSONObject(i)
                val originalUrl = bObj.getString("originalUrl")
                val normalizedUrl = bObj.optString("normalizedUrl", originalUrl)
                val title = bObj.optString("title", originalUrl)
                val description = bObj.optString("description").takeIf { it.isNotBlank() }
                val thumbnailUrl = bObj.optString("thumbnailUrl").takeIf { it.isNotBlank() }
                val sourceDomain = bObj.optString("sourceDomain", "web")
                val note = bObj.optString("note").takeIf { it.isNotBlank() }
                val isFavorite = bObj.optBoolean("isFavorite", false)
                val isArchived = bObj.optBoolean("isArchived", false)
                val isRead = bObj.optBoolean("isRead", false)
                val createdAt = bObj.optLong("createdAt", System.currentTimeMillis())
                val contentType = bObj.optString("contentType", "ARTICLE")
                val collectionName = bObj.optString("collectionName").takeIf { it.isNotBlank() }

                val tagNames = mutableListOf<String>()
                if (bObj.has("tags")) {
                    val tagsArray = bObj.getJSONArray("tags")
                    for (j in 0 until tagsArray.length()) {
                        val tName = tagsArray.getString(j).trim()
                        if (tName.isNotBlank()) {
                            tagNames.add(tName)
                            tagsMap[tName] = TagEntity(name = tName)
                        }
                    }
                }

                val entity = BookmarkEntity(
                    originalUrl = originalUrl,
                    normalizedUrl = normalizedUrl,
                    title = title,
                    description = description,
                    thumbnailUrl = thumbnailUrl,
                    sourceDomain = sourceDomain,
                    note = note,
                    isFavorite = isFavorite,
                    isArchived = isArchived,
                    isRead = isRead,
                    createdAt = createdAt,
                    contentType = contentType,
                    metadataStatus = "SUCCESS"
                )

                importedBookmarks.add(
                    ImportedBookmark(
                        bookmark = entity,
                        collectionName = collectionName,
                        tagNames = tagNames
                    )
                )
            }
        }

        return ImportedData(
            collections = collectionsList,
            tags = tagsMap.values.toList(),
            bookmarksWithDetails = importedBookmarks
        )
    }
}
