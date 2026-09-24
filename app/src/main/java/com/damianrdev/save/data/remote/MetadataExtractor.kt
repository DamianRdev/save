package com.damianrdev.save.data.remote

import com.damianrdev.save.core.common.UrlSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import javax.inject.Inject
import javax.inject.Singleton

data class ExtractedMetadata(
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val contentType: String,
    val author: String? = null,
    val readingTimeMinutes: Int = 1,
    val readerContent: String? = null
)

interface MetadataExtractor {
    suspend fun extract(url: String): ExtractedMetadata
}

@Singleton
class JsoupMetadataExtractor @Inject constructor() : MetadataExtractor {

    companion object {
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-S928B Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36"
        private const val META_CRAWLER_UA =
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"
        private const val TWITTER_CRAWLER_UA =
            "Twitterbot/1.0"
        private const val TIMEOUT_MS = 6000

        private val GENERIC_DESCRIPTIONS = listOf(
            "join threads to share ideas",
            "say more with threads",
            "see what people are saying",
            "log in with your instagram",
            "log in to threads",
            "create an account or log in to instagram",
            "see instagram photos and videos",
            "javascript is not available",
            "javascript is required",
            "don’t miss what’s happening",
            "don't miss what's happening",
            "people on x are the first to know"
        )

        private val GENERIC_TITLES = setOf(
            "threads",
            "threads • log in",
            "log in • threads",
            "instagram",
            "login • instagram",
            "x",
            "twitter",
            "facebook",
            "log in or sign up"
        )
    }

    override suspend fun extract(url: String): ExtractedMetadata = withContext(Dispatchers.IO) {
        val domain = UrlSanitizer.extractDomain(url)
        val defaultContentType = detectContentType(url, domain)
        val userAgent = selectUserAgent(domain)

        try {
            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer("https://www.google.com")
                .header("Accept-Language", "es-ES,es;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get()

            // Title extraction cascade
            val ogTitle = cleanText(doc.select("meta[property=og:title]").attr("content"))
            val twitterTitle = cleanText(doc.select("meta[name=twitter:title]").attr("content"))
            val docTitle = cleanText(doc.title())

            val rawTitle = when {
                ogTitle.isNotBlank() && !isGenericTitle(ogTitle) -> cleanSocialTitle(ogTitle)
                twitterTitle.isNotBlank() && !isGenericTitle(twitterTitle) -> cleanSocialTitle(twitterTitle)
                docTitle.isNotBlank() && !isGenericTitle(docTitle) -> cleanSocialTitle(docTitle)
                else -> ""
            }

            // Description extraction cascade
            val ogDesc = cleanText(doc.select("meta[property=og:description]").attr("content"))
            val twitterDesc = cleanText(doc.select("meta[name=twitter:description]").attr("content"))
            val metaDesc = cleanText(doc.select("meta[name=description]").attr("content"))

            val finalDesc = when {
                ogDesc.isNotBlank() && !isGenericDescription(ogDesc) -> cleanSocialDescription(ogDesc)
                twitterDesc.isNotBlank() && !isGenericDescription(twitterDesc) -> cleanSocialDescription(twitterDesc)
                metaDesc.isNotBlank() && !isGenericDescription(metaDesc) -> cleanSocialDescription(metaDesc)
                else -> null
            }

            // Author extraction
            val metaAuthor = cleanText(doc.select("meta[name=author]").attr("content"))
                .ifBlank { cleanText(doc.select("meta[property=article:author]").attr("content")) }
                .ifBlank { cleanText(doc.select("meta[name=twitter:creator]").attr("content")) }
                .ifBlank {
                    Regex("""@([A-Za-z0-9_.]+)""").find(url)?.groupValues?.getOrNull(1)?.let { "@$it" } ?: ""
                }

            // If title is generic or only shows author on Threads, enrich title with a snippet of the real post text
            val finalTitle = when {
                rawTitle.isNotBlank() && finalDesc != null && isAuthorOnlySocialTitle(rawTitle, domain) -> {
                    val snippet = finalDesc.lineSequence().firstOrNull { it.isNotBlank() }?.take(72)?.trim()
                    if (!snippet.isNullOrBlank()) {
                        val ellipsis = if (finalDesc.length > 72) "…" else ""
                        "$rawTitle: \"$snippet$ellipsis\""
                    } else {
                        rawTitle
                    }
                }
                rawTitle.isNotBlank() -> rawTitle
                finalDesc != null -> finalDesc.take(65).trim() + if (finalDesc.length > 65) "…" else ""
                else -> domain
            }

            // Thumbnail extraction cascade
            val ogImage = doc.select("meta[property=og:image]").attr("abs:content").trim()
                .ifBlank { doc.select("meta[property=og:image]").attr("content").trim() }
            val twitterImage = doc.select("meta[name=twitter:image]").attr("abs:content").trim()
                .ifBlank { doc.select("meta[name=twitter:image]").attr("content").trim() }
            val icon = doc.select("link[rel~=(?i)^(shortcut|icon)$]").attr("abs:href").trim()

            val finalImage = when {
                ogImage.isNotBlank() && !ogImage.contains("kHwIMM5b8PW.webp") -> decodeEntities(ogImage)
                twitterImage.isNotBlank() && !twitterImage.contains("kHwIMM5b8PW.webp") -> decodeEntities(twitterImage)
                icon.isNotBlank() -> decodeEntities(icon)
                else -> null
            }

            // Content type refinement
            val ogType = doc.select("meta[property=og:type]").attr("content").lowercase()
            val finalContentType = when {
                ogType.contains("video") -> "VIDEO"
                ogType.contains("article") && defaultContentType != "TWEET" -> "ARTICLE"
                else -> defaultContentType
            }

            // Clean Reader Content Extraction (Distraction-Free Article Text)
            doc.select("script, style, nav, header, footer, aside, iframe, noscript, form, .ads, .advertisement, .cookie, .popup").remove()
            val mainContainer = doc.select("article, [role=main], main, .post-content, .article-body, .entry-content").firstOrNull() ?: doc.body()
            val blocks = mutableListOf<String>()
            mainContainer?.select("h1, h2, h3, p, blockquote, li")?.forEach { el ->
                val text = cleanText(el.text())
                if (text.length >= 25 && !isGenericDescription(text)) {
                    val formatted = when (el.tagName().lowercase()) {
                        "h1", "h2", "h3" -> "## $text"
                        "blockquote" -> "> $text"
                        "li" -> "• $text"
                        else -> text
                    }
                    if (blocks.lastOrNull() != formatted) {
                        blocks.add(formatted)
                    }
                }
            }

            val extractedReaderBody = when {
                blocks.size >= 2 -> blocks.take(80).joinToString("\n\n")
                !finalDesc.isNullOrBlank() -> finalDesc
                else -> null
            }

            val totalWords = (extractedReaderBody ?: finalDesc ?: "").split(Regex("\\s+")).count { it.isNotBlank() }
            val readingMinutes = (totalWords / 190).coerceAtLeast(1)

            ExtractedMetadata(
                title = finalTitle,
                description = finalDesc,
                thumbnailUrl = finalImage,
                contentType = finalContentType,
                author = metaAuthor.ifBlank { null },
                readingTimeMinutes = readingMinutes,
                readerContent = extractedReaderBody
            )
        } catch (_: Exception) {
            ExtractedMetadata(
                title = domain,
                description = null,
                thumbnailUrl = null,
                contentType = defaultContentType,
                author = null,
                readingTimeMinutes = 1,
                readerContent = null
            )
        }
    }

    private fun selectUserAgent(domain: String): String {
        val lower = domain.lowercase()
        return when {
            lower.contains("threads.net") || lower.contains("threads.com") ||
            lower.contains("instagram.com") || lower.contains("facebook.com") -> META_CRAWLER_UA
            lower.contains("twitter.com") || lower.contains("x.com") -> TWITTER_CRAWLER_UA
            else -> DEFAULT_USER_AGENT
        }
    }

    private fun decodeEntities(input: String): String {
        if (input.isBlank()) return ""
        return try {
            Parser.unescapeEntities(input, false).trim()
        } catch (_: Exception) {
            input.trim()
        }
    }

    private fun cleanText(input: String): String = decodeEntities(input)

    private fun cleanSocialTitle(title: String): String {
        return title
            .removeSuffix(" • Threads, Say more")
            .removeSuffix(" on Threads")
            .removeSuffix(" / X")
            .trim()
    }

    private fun cleanSocialDescription(desc: String): String {
        // Remove trailing "See the latest conversations with @user." from profile bios if present
        return desc.replace(Regex("""\s*See the latest conversations with @[\w.]+\.?$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun isAuthorOnlySocialTitle(title: String, domain: String): Boolean {
        val lowerDomain = domain.lowercase()
        return (lowerDomain.contains("threads.") || lowerDomain.contains("x.com") || lowerDomain.contains("twitter.com")) &&
               title.contains("(@") && title.endsWith(")")
    }

    private fun isGenericTitle(title: String): Boolean {
        val normalized = title.lowercase().trim()
        return normalized.isBlank() || GENERIC_TITLES.contains(normalized)
    }

    private fun isGenericDescription(desc: String): Boolean {
        val normalized = desc.lowercase().trim()
        if (normalized.length < 4) return true
        return GENERIC_DESCRIPTIONS.any { normalized.contains(it) }
    }

    private fun detectContentType(url: String, domain: String): String {
        val lowerUrl = url.lowercase()
        val lowerDomain = domain.lowercase()
        return when {
            lowerDomain.contains("youtube.com") || lowerDomain.contains("youtu.be") ||
                    lowerDomain.contains("tiktok.com") || lowerDomain.contains("vimeo.com") -> "VIDEO"
            lowerDomain.contains("twitter.com") || lowerDomain.contains("x.com") ||
                    lowerDomain.contains("threads.net") || lowerDomain.contains("threads.com") -> "TWEET"
            lowerDomain.contains("instagram.com") -> "IMAGE"
            lowerDomain.contains("spotify.com") || lowerDomain.contains("soundcloud.com") -> "AUDIO"
            lowerUrl.endsWith(".pdf") -> "DOCUMENT"
            else -> "ARTICLE"
        }
    }
}
