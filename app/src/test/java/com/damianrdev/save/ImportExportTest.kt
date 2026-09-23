package com.damianrdev.save

import com.damianrdev.save.data.importer_exporter.CsvBackupHandler
import com.damianrdev.save.data.importer_exporter.HtmlBookmarkHandler
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import com.damianrdev.save.data.local.entities.CollectionEntity
import com.damianrdev.save.data.local.entities.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportExportTest {

    private val sampleBookmarks = listOf(
        BookmarkWithTagsAndCollection(
            bookmark = BookmarkEntity(
                id = 1L,
                originalUrl = "https://kotlinlang.org/docs/home.html",
                normalizedUrl = "https://kotlinlang.org/docs/home.html",
                title = "Kotlin Documentation",
                description = "Official Kotlin Docs",
                sourceDomain = "kotlinlang.org",
                note = "Referencia esencial",
                isFavorite = true
            ),
            collection = CollectionEntity(id = 10L, name = "Desarrollo"),
            tags = listOf(TagEntity(id = 100L, name = "kotlin"), TagEntity(id = 101L, name = "android"))
        )
    )

    @Test
    fun testCsvExportAndImportRoundTrip() {
        val csv = CsvBackupHandler.exportToCsv(sampleBookmarks)
        assertTrue(csv.contains("https://kotlinlang.org/docs/home.html"))
        assertTrue(csv.contains("Kotlin Documentation"))

        val imported = CsvBackupHandler.importFromCsv(csv)
        assertEquals(1, imported.size)
        assertEquals("https://kotlinlang.org/docs/home.html", imported[0].bookmark.originalUrl)
        assertEquals("Kotlin Documentation", imported[0].bookmark.title)
        assertEquals("Desarrollo", imported[0].collectionName)
    }

    @Test
    fun testHtmlExportAndImportRoundTrip() {
        val html = HtmlBookmarkHandler.exportToHtml(sampleBookmarks)
        assertTrue(html.contains("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue(html.contains("https://kotlinlang.org/docs/home.html"))

        val imported = HtmlBookmarkHandler.importFromHtml(html)
        assertEquals(1, imported.size)
        assertEquals("https://kotlinlang.org/docs/home.html", imported[0].bookmark.originalUrl)
        assertEquals("Kotlin Documentation", imported[0].bookmark.title)
    }
}
