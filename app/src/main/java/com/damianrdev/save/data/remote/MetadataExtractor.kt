package com.damianrdev.save.data.remote

import com.damianrdev.save.core.common.UrlSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

data class ExtractedMetadata(
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val contentType: String
)

interface MetadataExtractor {
    suspend fun extract(url: String): ExtractedMetadata
}

@Singleton
class JsoupMetadataExtractor @Inject constructor() : MetadataExtractor {

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-S928B Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36"
        private const val TIMEOUT_MS = 5000
    }

    override suspend fun extract(url: String): ExtractedMetadata = withContext(Dispatchers.IO) {
        val domain = UrlSanitizer.extractDomain(url)
        val defaultContentType = detectContentType(url, domain)

        try {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get()

            // Title extraction cascade
            val ogTitle = doc.select("meta[property=og:title]").attr("content").trim()
            val twitterTitle = doc.select("meta[name=twitter:title]").attr("content").trim()
            val docTitle = doc.title().trim()
            val finalTitle = when {
                ogTitle.isNotBlank() -> ogTitle
                twitterTitle.isNotBlank() -> twitterTitle
                docTitle.isNotBlank() -> docTitle
                else -> domain
            }

            // Description extraction cascade
            val ogDesc = doc.select("meta[property=og:description]").attr("content").trim()
            val twitterDesc = doc.select("meta[name=twitter:description]").attr("content").trim()
            val metaDesc = doc.select("meta[name=description]").attr("content").trim()
            val finalDesc = when {
                ogDesc.isNotBlank() -> ogDesc
                twitterDesc.isNotBlank() -> twitterDesc
                metaDesc.isNotBlank() -> metaDesc
                else -> null
            }

            // Thumbnail extraction cascade
            val ogImage = doc.select("meta[property=og:image]").attr("abs:content").trim()
            val twitterImage = doc.select("meta[name=twitter:image]").attr("abs:content").trim()
            val icon = doc.select("link[rel~=(?i)^(shortcut|icon)$]").attr("abs:href").trim()
            val finalImage = when {
                ogImage.isNotBlank() -> ogImage
                twitterImage.isNotBlank() -> twitterImage
                icon.isNotBlank() -> icon
                else -> null
            }

            // Content type refinement
            val ogType = doc.select("meta[property=og:type]").attr("content").lowercase()
            val finalContentType = when {
                ogType.contains("video") -> "VIDEO"
                ogType.contains("article") -> "ARTICLE"
                else -> defaultContentType
            }

            ExtractedMetadata(
                title = finalTitle,
                description = finalDesc,
                thumbnailUrl = finalImage,
                contentType = finalContentType
            )
        } catch (_: Exception) {
            // Safe fallback if offline, timeout or blocked by bot detection
            ExtractedMetadata(
                title = domain,
                description = null,
                thumbnailUrl = null,
                contentType = defaultContentType
            )
        }
    }

    private fun detectContentType(url: String, domain: String): String {
        val lowerUrl = url.lowercase()
        val lowerDomain = domain.lowercase()
        return when {
            lowerDomain.contains("youtube.com") || lowerDomain.contains("youtu.be") ||
                    lowerDomain.contains("tiktok.com") || lowerDomain.contains("vimeo.com") -> "VIDEO"
            lowerDomain.contains("twitter.com") || lowerDomain.contains("x.com") -> "TWEET"
            lowerDomain.contains("instagram.com") -> "IMAGE"
            lowerDomain.contains("spotify.com") || lowerDomain.contains("soundcloud.com") -> "AUDIO"
            lowerUrl.endsWith(".pdf") -> "DOCUMENT"
            else -> "ARTICLE"
        }
    }
}
