package com.damianrdev.save.data.importer_exporter

import com.damianrdev.save.core.common.UrlSanitizer
import com.damianrdev.save.data.local.entities.BookmarkEntity
import com.damianrdev.save.data.local.entities.BookmarkWithTagsAndCollection
import java.io.BufferedReader
import java.io.StringReader

object CsvBackupHandler {

    private const val HEADER = "url,title,description,domain,note,collection,tags,is_favorite,created_at"

    fun exportToCsv(bookmarks: List<BookmarkWithTagsAndCollection>): String {
        val sb = StringBuilder()
        sb.append(HEADER).append("\n")

        bookmarks.forEach { item ->
            val b = item.bookmark
            val tags = item.tags.joinToString(";") { it.name }
            val collection = item.collection?.name ?: ""

            sb.append(escape(b.originalUrl)).append(",")
            sb.append(escape(b.title)).append(",")
            sb.append(escape(b.description ?: "")).append(",")
            sb.append(escape(b.sourceDomain)).append(",")
            sb.append(escape(b.note ?: "")).append(",")
            sb.append(escape(collection)).append(",")
            sb.append(escape(tags)).append(",")
            sb.append(if (b.isFavorite) "1" else "0").append(",")
            sb.append(b.createdAt).append("\n")
        }

        return sb.toString()
    }

    fun importFromCsv(csvContent: String): List<ImportedBookmark> {
        val results = mutableListOf<ImportedBookmark>()
        val reader = BufferedReader(StringReader(csvContent))
        var line = reader.readLine() ?: return results

        // Check if first line is header
        if (!line.lowercase().contains("url")) {
            // Treat as data if not header
            parseLine(line)?.let { results.add(it) }
        }

        while (true) {
            line = reader.readLine() ?: break
            if (line.isBlank()) continue
            parseLine(line)?.let { results.add(it) }
        }

        return results
    }

    private fun parseLine(line: String): ImportedBookmark? {
        val tokens = parseCsvLine(line)
        if (tokens.isEmpty()) return null
        val url = tokens.getOrNull(0) ?: return null
        if (!url.startsWith("http://") && !url.startsWith("https://")) return null

        val title = tokens.getOrNull(1)?.ifBlank { null } ?: UrlSanitizer.extractDomain(url)
        val description = tokens.getOrNull(2)?.ifBlank { null }
        val domain = tokens.getOrNull(3)?.ifBlank { null } ?: UrlSanitizer.extractDomain(url)
        val note = tokens.getOrNull(4)?.ifBlank { null }
        val collection = tokens.getOrNull(5)?.ifBlank { null }
        val tagsString = tokens.getOrNull(6) ?: ""
        val isFavorite = tokens.getOrNull(7) == "1" || tokens.getOrNull(7).equals("true", true)
        val createdAt = tokens.getOrNull(8)?.toLongOrNull() ?: System.currentTimeMillis()

        val tagNames = tagsString.split(";", ",").map { it.trim() }.filter { it.isNotBlank() }

        val bookmark = BookmarkEntity(
            originalUrl = url,
            normalizedUrl = UrlSanitizer.cleanTrackingParameters(url),
            title = title,
            description = description,
            sourceDomain = domain,
            note = note,
            isFavorite = isFavorite,
            createdAt = createdAt,
            metadataStatus = "SUCCESS"
        )

        return ImportedBookmark(bookmark, collection, tagNames)
    }

    private fun escape(data: String): String {
        var escaped = data.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString().trim())
                cur.clear()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString().trim())
        return result
    }
}
