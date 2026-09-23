package com.damianrdev.save.data.importer_exporter

import com.damianrdev.save.core.common.UrlSanitizer
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import org.jsoup.Jsoup

object HtmlBookmarkHandler {

    fun exportToHtml(bookmarks: List<BookmarkWithTagsAndCollection>): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
        sb.append("<!-- This is an automatically generated file. It will be read and overwritten. Do Not Edit! -->\n")
        sb.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n")
        sb.append("<TITLE>Bookmarks</TITLE>\n")
        sb.append("<H1>Bookmarks</H1>\n")
        sb.append("<DL><p>\n")

        // Group by collection
        val grouped = bookmarks.groupBy { it.collection?.name ?: "Sin Colección" }
        grouped.forEach { (collectionName, items) ->
            sb.append("    <DT><H3 ADD_DATE=\"${System.currentTimeMillis() / 1000}\">$collectionName</H3>\n")
            sb.append("    <DL><p>\n")
            items.forEach { item ->
                val b = item.bookmark
                val tags = item.tags.joinToString(",") { it.name }
                val addDate = b.createdAt / 1000
                sb.append("        <DT><A HREF=\"${b.originalUrl}\" ADD_DATE=\"$addDate\" TAGS=\"$tags\">${escapeXml(b.title)}</A>\n")
                if (!b.note.isNullOrBlank()) {
                    sb.append("        <DD>${escapeXml(b.note)}\n")
                }
            }
            sb.append("    </DL><p>\n")
        }

        sb.append("</DL><p>\n")
        return sb.toString()
    }

    fun importFromHtml(htmlContent: String): List<ImportedBookmark> {
        val results = mutableListOf<ImportedBookmark>()
        val doc = Jsoup.parse(htmlContent)
        val links = doc.select("a[href]")

        for (link in links) {
            val url = link.attr("href").trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) continue

            val title = link.text().trim().ifBlank { UrlSanitizer.extractDomain(url) }
            val tagsAttr = link.attr("tags")
            val addDateAttr = link.attr("add_date").toLongOrNull()
            val createdAt = if (addDateAttr != null && addDateAttr > 0) addDateAttr * 1000 else System.currentTimeMillis()

            // Find closest parent DL -> previous H3 for collection name
            var collectionName: String? = null
            var parent = link.parent()
            while (parent != null) {
                if (parent.tagName().equals("dl", ignoreCase = true)) {
                    val prev = parent.previousElementSibling()
                    if (prev != null && prev.tagName().equals("h3", ignoreCase = true)) {
                        collectionName = prev.text().trim()
                        break
                    }
                }
                parent = parent.parent()
            }

            val tagNames = if (tagsAttr.isNotBlank()) {
                tagsAttr.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                emptyList()
            }

            val bookmark = BookmarkEntity(
                originalUrl = url,
                normalizedUrl = UrlSanitizer.cleanTrackingParameters(url),
                title = title,
                sourceDomain = UrlSanitizer.extractDomain(url),
                createdAt = createdAt,
                metadataStatus = "SUCCESS"
            )

            results.add(ImportedBookmark(bookmark, collectionName, tagNames))
        }

        return results
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
