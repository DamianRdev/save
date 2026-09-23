package com.damianrdev.save.core.common

import java.net.URI

object UrlSanitizer {

    private val URL_REGEX = Regex(
        """(https?://[a-zA-Z0-9\-._~:/?#\[\]@!$&'()*+,;=%]+)""",
        RegexOption.IGNORE_CASE
    )

    private val TRACKING_PARAMS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "si", "igsh", "fbclid", "gclid", "msclkid", "mc_eid", "_ga", "_gl",
        "ref", "ref_src", "source"
    )

    /**
     * Extracts the first valid HTTP/HTTPS URL from a given text payload.
     * Supports messy text shared from TikTok, Instagram, Twitter/X, YouTube, etc.
     */
    fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val match = URL_REGEX.find(text) ?: return null
        var url = match.value.trim()
        
        // Clean trailing punctuation that might get accidentally caught from sentences
        while (url.isNotEmpty() && (url.endsWith(".") || url.endsWith(",") || url.endsWith(")") || url.endsWith("]"))) {
            url = url.substring(0, url.length - 1)
        }
        return if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
            url
        } else {
            null
        }
    }

    /**
     * Cleans tracking parameters from the URL for privacy and deduplication.
     */
    fun cleanTrackingParameters(url: String): String {
        return try {
            val uri = URI(url)
            val rawQuery = uri.rawQuery ?: return url
            val queryPairs = rawQuery.split("&")
            val filteredPairs = queryPairs.filterNot { pair ->
                val key = pair.substringBefore("=").lowercase()
                TRACKING_PARAMS.contains(key)
            }
            val newQuery = if (filteredPairs.isNotEmpty()) "?${filteredPairs.joinToString("&")}" else ""
            val scheme = uri.scheme ?: "https"
            val authority = uri.rawAuthority ?: ""
            val path = uri.rawPath ?: ""
            val fragment = if (uri.rawFragment != null) "#${uri.rawFragment}" else ""
            "$scheme://$authority$path$newQuery$fragment"
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Extracts a clean host domain (e.g., 'youtube.com', 'instagram.com')
     */
    fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host ?: return url
            if (host.startsWith("www.", ignoreCase = true)) host.substring(4) else host
        } catch (_: Exception) {
            url.substringBefore("/").removePrefix("http://").removePrefix("https://")
        }
    }
}
