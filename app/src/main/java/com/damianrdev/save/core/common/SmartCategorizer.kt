package com.damianrdev.save.core.common

import java.net.URI

data class CategoryInference(
    val name: String,
    val colorHex: String,
    val iconName: String,
    val contentType: String,
    val defaultTags: List<String> = emptyList(),
    val summaryHint: String,
    val typeBadge: String
)

object SmartCategorizer {

    fun inferCategory(url: String, domain: String, title: String? = null): CategoryInference {
        val lowerUrl = url.lowercase()
        val lowerDomain = domain.lowercase()
        val lowerTitle = title?.lowercase().orEmpty()

        return when {
            // Videos & Streaming
            lowerDomain.contains("youtube.com") || lowerDomain.contains("youtu.be") ||
            lowerDomain.contains("tiktok.com") || lowerDomain.contains("vimeo.com") ||
            lowerDomain.contains("twitch.tv") || lowerDomain.contains("dailymotion.com") ||
            lowerUrl.contains("/video/") || lowerUrl.contains("/watch") -> {
                CategoryInference(
                    name = "Videos",
                    colorHex = "#EF4444",
                    iconName = "play_arrow",
                    contentType = "VIDEO",
                    defaultTags = listOf("video", "multimedia"),
                    summaryHint = "Video o transmisión multimedia",
                    typeBadge = "🎥 Video"
                )
            }

            // Code & Development
            lowerDomain.contains("github.com") || lowerDomain.contains("gitlab.com") ||
            lowerDomain.contains("stackoverflow.com") || lowerDomain.contains("dev.to") ||
            lowerDomain.contains("developer.android.com") || lowerDomain.contains("kotlinlang.org") ||
            lowerDomain.contains("npmjs.com") || lowerDomain.contains("pypi.org") ||
            lowerDomain.contains("hackernews") || lowerDomain.contains("news.ycombinator.com") ||
            lowerUrl.contains("/repo") || lowerUrl.contains("/commit/") ||
            lowerTitle.contains("github") || lowerTitle.contains("tutorial") -> {
                CategoryInference(
                    name = "Desarrollo",
                    colorHex = "#10B981",
                    iconName = "code",
                    contentType = "CODE",
                    defaultTags = listOf("tech", "codigo"),
                    summaryHint = "Código, repositorio o documentación técnica",
                    typeBadge = "💻 Código"
                )
            }

            // Social Networks
            lowerDomain.contains("twitter.com") || lowerDomain.contains("x.com") ||
            lowerDomain.contains("instagram.com") || lowerDomain.contains("threads.net") ||
            lowerDomain.contains("reddit.com") || lowerDomain.contains("linkedin.com") ||
            lowerDomain.contains("bsky.app") || lowerDomain.contains("facebook.com") -> {
                CategoryInference(
                    name = "Redes Sociales",
                    colorHex = "#3B82F6",
                    iconName = "share",
                    contentType = "TWEET",
                    defaultTags = listOf("social"),
                    summaryHint = "Publicación o hilo en red social",
                    typeBadge = "🐦 Social"
                )
            }

            // Shopping & E-Commerce
            lowerDomain.contains("amazon.") || lowerDomain.contains("mercadolibre.") ||
            lowerDomain.contains("aliexpress.com") || lowerDomain.contains("ebay.com") ||
            lowerDomain.contains("shein.com") || lowerDomain.contains("walmart.com") ||
            lowerDomain.contains("tiendamia.com") || lowerUrl.contains("/product/") ||
            lowerUrl.contains("/articulo/") || lowerUrl.contains("/item/") -> {
                CategoryInference(
                    name = "Compras",
                    colorHex = "#EC4899",
                    iconName = "shopping_bag",
                    contentType = "PRODUCT",
                    defaultTags = listOf("compras", "wishlist"),
                    summaryHint = "Producto o tienda de comercio electrónico",
                    typeBadge = "🛍️ Compra"
                )
            }

            // News & Current Events
            lowerDomain.contains("bbc.com") || lowerDomain.contains("cnn.com") ||
            lowerDomain.contains("elpais.com") || lowerDomain.contains("infobae.com") ||
            lowerDomain.contains("clarin.com") || lowerDomain.contains("lanacion.com") ||
            lowerDomain.contains("theguardian.com") || lowerDomain.contains("reuters.com") ||
            lowerDomain.contains("nytimes.com") || lowerDomain.contains("noticias") -> {
                CategoryInference(
                    name = "Noticias",
                    colorHex = "#F59E0B",
                    iconName = "newspaper",
                    contentType = "ARTICLE",
                    defaultTags = listOf("noticias"),
                    summaryHint = "Artículo periodístico o de actualidad",
                    typeBadge = "📰 Noticia"
                )
            }

            // Music & Podcasts
            lowerDomain.contains("spotify.com") || lowerDomain.contains("soundcloud.com") ||
            lowerDomain.contains("music.apple.com") || lowerDomain.contains("ivoox.com") ||
            lowerDomain.contains("podcast") -> {
                CategoryInference(
                    name = "Música & Podcasts",
                    colorHex = "#8B5CF6",
                    iconName = "headphones",
                    contentType = "AUDIO",
                    defaultTags = listOf("musica", "audio"),
                    summaryHint = "Audio, pista musical o episodio de podcast",
                    typeBadge = "🎵 Audio"
                )
            }

            // Recipes & Cooking
            lowerUrl.contains("receta") || lowerUrl.contains("recipe") ||
            lowerDomain.contains("cook") || lowerDomain.contains("recetas") -> {
                CategoryInference(
                    name = "Recetas",
                    colorHex = "#F97316",
                    iconName = "restaurant",
                    contentType = "ARTICLE",
                    defaultTags = listOf("cocina", "recetas"),
                    summaryHint = "Receta culinaria o preparación gastronómica",
                    typeBadge = "🍳 Receta"
                )
            }

            // Design & Visual Resources
            lowerDomain.contains("figma.com") || lowerDomain.contains("dribbble.com") ||
            lowerDomain.contains("behance.net") || lowerDomain.contains("pinterest.com") ||
            lowerDomain.contains("canva.com") || lowerDomain.contains("unsplash.com") -> {
                CategoryInference(
                    name = "Diseño",
                    colorHex = "#06B6D4",
                    iconName = "palette",
                    contentType = "IMAGE",
                    defaultTags = listOf("diseño", "arte"),
                    summaryHint = "Inspiración de diseño o recurso visual",
                    typeBadge = "🎨 Diseño"
                )
            }

            // Documents & Notes
            lowerUrl.endsWith(".pdf") || lowerDomain.contains("drive.google.com") ||
            lowerDomain.contains("notion.so") || lowerDomain.contains("docs.google.com") -> {
                CategoryInference(
                    name = "Documentos",
                    colorHex = "#6366F1",
                    iconName = "description",
                    contentType = "DOCUMENT",
                    defaultTags = listOf("documento"),
                    summaryHint = "Documento digital, nota o archivo descargable",
                    typeBadge = "📄 Documento"
                )
            }

            // Reading, Articles & Blogs
            lowerDomain.contains("medium.com") || lowerDomain.contains("substack.com") ||
            lowerDomain.contains("wikipedia.org") || lowerUrl.contains("/blog/") ||
            lowerUrl.contains("/article/") || lowerUrl.contains("/post/") -> {
                CategoryInference(
                    name = "Lectura",
                    colorHex = "#3B82F6",
                    iconName = "menu_book",
                    contentType = "ARTICLE",
                    defaultTags = listOf("lectura"),
                    summaryHint = "Artículo, publicación o texto de lectura",
                    typeBadge = "📖 Lectura"
                )
            }

            // Fallback General
            else -> {
                CategoryInference(
                    name = "General",
                    colorHex = "#64748B",
                    iconName = "bookmark",
                    contentType = "ARTICLE",
                    defaultTags = emptyList(),
                    summaryHint = "Enlace web guardado en tu biblioteca",
                    typeBadge = "🌐 Enlace"
                )
            }
        }
    }

    /**
     * Generates a readable, informative title directly from the URL or shared text without internet.
     */
    fun generateSmartTitle(url: String, domain: String, rawSharedText: String? = null): String {
        // If raw shared text had a title alongside the URL (e.g. from YouTube/Twitter/Chrome share)
        if (!rawSharedText.isNullOrBlank()) {
            val textWithoutUrl = rawSharedText.replace(url, "").trim()
            val clean = textWithoutUrl
                .removePrefix("Mira esto:")
                .removePrefix("Check out:")
                .removePrefix("Te comparto:")
                .trim(' ', '-', '—', ':', '\n', '\r', '"')
            if (clean.length in 4..120) {
                return clean
            }
        }

        // Try extracting human-readable words from the URL path slug
        try {
            val uri = URI(url)
            val path = uri.path?.trim('/') ?: ""
            if (path.isNotBlank()) {
                val segments = path.split('/').filter { it.isNotBlank() }
                if (segments.isNotEmpty()) {
                    val lastSegment = segments.last()
                        .removeSuffix(".html")
                        .removeSuffix(".php")
                        .removeSuffix(".htm")
                    val words = lastSegment
                        .replace('-', ' ')
                        .replace('_', ' ')
                        .split(' ')
                        .filter { it.isNotBlank() && it.length > 1 }

                    if (words.isNotEmpty()) {
                        val capitalized = words.joinToString(" ") { word ->
                            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                        return "$capitalized • $domain"
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore URI parsing issues and fallback
        }

        return domain
    }

    /**
     * Generates a clear contextual summary at a glance if the web does not provide description or if offline.
     */
    fun generateSmartDescription(url: String, domain: String, inference: CategoryInference): String {
        return "${inference.summaryHint} guardado desde $domain"
    }
}
