package com.damianrdev.save.data.repository

import com.damianrdev.save.core.common.NetworkObserver
import com.damianrdev.save.core.common.SmartCategorizer
import com.damianrdev.save.core.common.UrlSanitizer
import com.damianrdev.save.data.importer_exporter.CsvBackupHandler
import com.damianrdev.save.data.importer_exporter.HtmlBookmarkHandler
import com.damianrdev.save.data.importer_exporter.ImportedBookmark
import com.damianrdev.save.data.importer_exporter.JsonBackupHandler
import com.damianrdev.save.data.local.dao.BookmarkDao
import com.damianrdev.save.data.local.dao.CollectionDao
import com.damianrdev.save.data.local.dao.TagDao
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import com.damianrdev.save.data.local.entities.CollectionEntity
import com.damianrdev.save.data.local.entities.TagEntity
import com.damianrdev.save.data.remote.MetadataExtractor
import com.damianrdev.save.domain.model.Bookmark
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.model.SearchFilter
import com.damianrdev.save.domain.model.Tag
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.ExportFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val metadataExtractor: MetadataExtractor,
    private val networkObserver: NetworkObserver
) : BookmarkRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getAllActiveBookmarks(): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getAllActiveBookmarks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getBookmarkById(id: Long): Flow<BookmarkWithDetails?> {
        return bookmarkDao.getBookmarkById(id).map { it?.toDomain() }
    }

    override fun getFavorites(): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getFavoriteBookmarks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getArchived(): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getArchivedBookmarks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTrash(): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getTrashBookmarks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getByCollection(collectionId: Long): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getBookmarksByCollection(collectionId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUncategorized(): Flow<List<BookmarkWithDetails>> {
        return bookmarkDao.getUncategorizedBookmarks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun search(filter: SearchFilter): Flow<List<BookmarkWithDetails>> {
        val baseQuery = filter.query.trim()
        val flow = if (baseQuery.isBlank()) {
            bookmarkDao.getAllActiveBookmarks()
        } else {
            bookmarkDao.searchBookmarks(baseQuery)
        }

        return flow.map { list ->
            list.map { it.toDomain() }.filter { item ->
                val b = item.bookmark
                var matches = true
                if (filter.onlyFavorites && !b.isFavorite) matches = false
                if (filter.onlyArchived && !b.isArchived) matches = false
                if (filter.onlyUnread && b.isRead) matches = false
                if (filter.collectionId != null && b.collectionId != filter.collectionId) matches = false
                if (filter.tagId != null && item.tags.none { it.id == filter.tagId }) matches = false
                if (!filter.sourceDomain.isNullOrBlank() && !b.sourceDomain.equals(filter.sourceDomain, ignoreCase = true)) matches = false
                matches
            }
        }
    }

    override suspend fun saveBookmark(
        originalUrl: String,
        title: String?,
        note: String?,
        collectionId: Long?,
        tagNames: List<String>,
        rawSharedText: String?
    ): Long {
        val normalizedUrl = UrlSanitizer.cleanTrackingParameters(originalUrl)
        val domain = UrlSanitizer.extractDomain(originalUrl)

        // Smart categorization inference
        val inference = SmartCategorizer.inferCategory(originalUrl, domain, title)

        // Resolve or auto-create collection if not specified
        val resolvedCollectionId = if (collectionId != null) {
            collectionId
        } else {
            val existing = collectionDao.getCollectionByName(inference.name)
            existing?.id ?: collectionDao.insertCollection(
                CollectionEntity(
                    name = inference.name,
                    colorHex = inference.colorHex,
                    iconName = inference.iconName
                )
            )
        }

        // Smart title and description fallback (ideal for offline or immediate preview)
        val smartTitle = title?.ifBlank { null } ?: SmartCategorizer.generateSmartTitle(originalUrl, domain, rawSharedText ?: note)
        val smartDesc = SmartCategorizer.generateSmartDescription(originalUrl, domain, inference, rawSharedText)

        val isOnline = networkObserver.isOnline
        val initialStatus = if (isOnline) "PENDING" else "OFFLINE"

        val entity = BookmarkEntity(
            originalUrl = originalUrl,
            normalizedUrl = normalizedUrl,
            title = smartTitle,
            description = smartDesc,
            sourceDomain = domain,
            note = note,
            collectionId = resolvedCollectionId,
            contentType = inference.contentType,
            metadataStatus = initialStatus
        )

        val bookmarkId = bookmarkDao.insertBookmark(entity)

        // Associate tags (use provided tags or fallback to smart default tags)
        val effectiveTags = if (tagNames.isNotEmpty()) tagNames else inference.defaultTags
        if (effectiveTags.isNotEmpty()) {
            val tagIds = effectiveTags.mapNotNull { name ->
                val trimmed = name.trim().removePrefix("#")
                if (trimmed.isNotBlank()) tagDao.getOrCreateTag(trimmed).id else null
            }
            bookmarkDao.updateBookmarkTags(bookmarkId, tagIds)
        }

        // If online, asynchronously fetch rich OpenGraph metadata
        if (isOnline) {
            repositoryScope.launch {
                try {
                    val metadata = metadataExtractor.extract(originalUrl)
                    val fetchedTitleIsUseful = metadata.title.isNotBlank() && !metadata.title.equals(domain, ignoreCase = true)
                    val finalTitle = when {
                        !title.isNullOrBlank() -> title
                        fetchedTitleIsUseful -> metadata.title
                        else -> smartTitle
                    }
                    val finalDesc = metadata.description?.ifBlank { null } ?: smartDesc
                    bookmarkDao.updateMetadata(
                        id = bookmarkId,
                        status = "SUCCESS",
                        title = finalTitle,
                        description = finalDesc,
                        thumbnailUrl = metadata.thumbnailUrl
                    )
                } catch (_: Exception) {
                    bookmarkDao.updateMetadata(
                        id = bookmarkId,
                        status = "FAILED",
                        title = smartTitle,
                        description = smartDesc,
                        thumbnailUrl = null
                    )
                }
            }
        }

        return bookmarkId
    }

    override suspend fun updateBookmark(bookmark: Bookmark, tagNames: List<String>) {
        val entity = bookmark.toEntity()
        bookmarkDao.updateBookmark(entity)

        val tagIds = tagNames.mapNotNull { name ->
            val trimmed = name.trim()
            if (trimmed.isNotBlank()) tagDao.getOrCreateTag(trimmed).id else null
        }
        bookmarkDao.updateBookmarkTags(bookmark.id, tagIds)
    }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        bookmarkDao.setFavorite(id, isFavorite)
    }

    override suspend fun setRead(id: Long, isRead: Boolean) {
        bookmarkDao.setRead(id, isRead)
    }

    override suspend fun moveToTrash(ids: List<Long>) {
        bookmarkDao.moveToTrash(ids)
    }

    override suspend fun restoreFromTrash(ids: List<Long>) {
        bookmarkDao.restoreFromTrash(ids)
    }

    override suspend fun deletePermanently(ids: List<Long>) {
        bookmarkDao.deletePermanently(ids)
    }

    override suspend fun emptyTrash() {
        bookmarkDao.emptyTrash()
    }

    override suspend fun setArchived(ids: List<Long>, isArchived: Boolean) {
        bookmarkDao.setArchived(ids, isArchived)
    }

    override suspend fun moveToCollection(ids: List<Long>, collectionId: Long?) {
        bookmarkDao.moveToCollection(ids, collectionId)
    }

    override suspend fun refreshMetadata(bookmarkId: Long) {
        val existing = bookmarkDao.getBookmarkByIdSync(bookmarkId) ?: return
        val b = existing.bookmark
        repositoryScope.launch {
            try {
                val metadata = metadataExtractor.extract(b.originalUrl)
                val inference = SmartCategorizer.inferCategory(b.originalUrl, b.sourceDomain, b.title)
                val smartTitle = SmartCategorizer.generateSmartTitle(b.originalUrl, b.sourceDomain, b.note)
                val smartDesc = SmartCategorizer.generateSmartDescription(b.originalUrl, b.sourceDomain, inference)

                val fetchedTitleIsUseful = metadata.title.isNotBlank() && !metadata.title.equals(b.sourceDomain, ignoreCase = true)
                val finalTitle = if (fetchedTitleIsUseful) metadata.title else smartTitle
                val finalDesc = metadata.description?.ifBlank { null } ?: smartDesc

                bookmarkDao.updateMetadata(
                    id = bookmarkId,
                    status = "SUCCESS",
                    title = finalTitle,
                    description = finalDesc,
                    thumbnailUrl = metadata.thumbnailUrl ?: b.thumbnailUrl
                )
            } catch (_: Exception) {
                // Ignore failure
            }
        }
    }

    override suspend fun exportData(format: ExportFormat): String {
        val allBookmarks = bookmarkDao.getAllActiveBookmarksSync()
        val allCollections = collectionDao.getAllCollectionsSync()

        return when (format) {
            ExportFormat.JSON -> JsonBackupHandler.exportToJson(allBookmarks, allCollections)
            ExportFormat.CSV -> CsvBackupHandler.exportToCsv(allBookmarks)
            ExportFormat.HTML -> HtmlBookmarkHandler.exportToHtml(allBookmarks)
        }
    }

    override suspend fun importData(content: String, format: ExportFormat): Int {
        val importedList: List<ImportedBookmark> = when (format) {
            ExportFormat.JSON -> {
                val data = JsonBackupHandler.importFromJson(content)
                // Insert collections first
                data.collections.forEach { col ->
                    if (collectionDao.getCollectionByName(col.name) == null) {
                        collectionDao.insertCollection(col)
                    }
                }
                data.bookmarksWithDetails
            }
            ExportFormat.CSV -> CsvBackupHandler.importFromCsv(content)
            ExportFormat.HTML -> HtmlBookmarkHandler.importFromHtml(content)
        }

        var importedCount = 0
        for (item in importedList) {
            val collectionId = if (!item.collectionName.isNullOrBlank()) {
                val existing = collectionDao.getCollectionByName(item.collectionName)
                existing?.id ?: collectionDao.insertCollection(
                    CollectionEntity(name = item.collectionName)
                )
            } else null

            val bookmarkToInsert = item.bookmark.copy(collectionId = collectionId)
            val bookmarkId = bookmarkDao.insertBookmark(bookmarkToInsert)

            val tagIds = item.tagNames.map { tagName ->
                tagDao.getOrCreateTag(tagName).id
            }
            bookmarkDao.updateBookmarkTags(bookmarkId, tagIds)
            importedCount++
        }

        return importedCount
    }

    // Mappers
    private fun BookmarkWithTagsAndCollection.toDomain(): BookmarkWithDetails {
        return BookmarkWithDetails(
            bookmark = bookmark.toDomain(),
            collection = collection?.let {
                Collection(
                    id = it.id,
                    name = it.name,
                    colorHex = it.colorHex,
                    iconName = it.iconName,
                    createdAt = it.createdAt
                )
            },
            tags = tags.map { Tag(id = it.id, name = it.name) }
        )
    }

    private fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            id = id,
            originalUrl = originalUrl,
            normalizedUrl = normalizedUrl,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            sourceDomain = sourceDomain,
            sourceApp = sourceApp,
            note = note,
            isFavorite = isFavorite,
            isArchived = isArchived,
            isDeleted = isDeleted,
            isRead = isRead,
            createdAt = createdAt,
            updatedAt = updatedAt,
            collectionId = collectionId,
            metadataStatus = metadataStatus,
            contentType = contentType
        )
    }

    private fun Bookmark.toEntity(): BookmarkEntity {
        return BookmarkEntity(
            id = id,
            originalUrl = originalUrl,
            normalizedUrl = normalizedUrl,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            sourceDomain = sourceDomain,
            sourceApp = sourceApp,
            note = note,
            isFavorite = isFavorite,
            isArchived = isArchived,
            isDeleted = isDeleted,
            isRead = isRead,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis(),
            collectionId = collectionId,
            metadataStatus = metadataStatus,
            contentType = contentType
        )
    }
}
