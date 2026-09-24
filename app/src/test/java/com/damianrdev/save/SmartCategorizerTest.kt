package com.damianrdev.save

import com.damianrdev.save.core.common.SmartCategorizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartCategorizerTest {

    @Test
    fun testCategorizeVideos() {
        val cat1 = SmartCategorizer.inferCategory("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "youtube.com")
        assertEquals("Videos", cat1.name)
        assertEquals("VIDEO", cat1.contentType)

        val cat2 = SmartCategorizer.inferCategory("https://tiktok.com/@user/video/12345", "tiktok.com")
        assertEquals("Videos", cat2.name)
    }

    @Test
    fun testCategorizeCodeAndTech() {
        val cat1 = SmartCategorizer.inferCategory("https://github.com/DamianRdev/save", "github.com")
        assertEquals("Desarrollo", cat1.name)
        assertEquals("CODE", cat1.contentType)

        val cat2 = SmartCategorizer.inferCategory("https://stackoverflow.com/questions/12345/kotlin", "stackoverflow.com")
        assertEquals("Desarrollo", cat2.name)
    }

    @Test
    fun testCategorizeSocialMedia() {
        val cat1 = SmartCategorizer.inferCategory("https://x.com/android/status/12345", "x.com")
        assertEquals("Redes Sociales", cat1.name)

        val cat2 = SmartCategorizer.inferCategory("https://www.instagram.com/p/Cxyz/", "instagram.com")
        assertEquals("Redes Sociales", cat2.name)
    }

    @Test
    fun testCategorizeShopping() {
        val cat = SmartCategorizer.inferCategory("https://www.mercadolibre.com.ar/articulo/MLA123", "mercadolibre.com.ar")
        assertEquals("Compras", cat.name)
        assertEquals("PRODUCT", cat.contentType)
    }

    @Test
    fun testCategorizeNews() {
        val cat = SmartCategorizer.inferCategory("https://www.bbc.com/news/technology-12345", "bbc.com")
        assertEquals("Noticias", cat.name)
    }

    @Test
    fun testGenerateSmartTitleFromSharedText() {
        val sharedText = "Kotlin 2.0 Released: Everything You Need to Know https://dev.to/kotlin-2"
        val url = "https://dev.to/kotlin-2"
        val title = SmartCategorizer.generateSmartTitle(url, "dev.to", sharedText)
        assertEquals("Kotlin 2.0 Released: Everything You Need to Know", title)
    }

    @Test
    fun testGenerateSmartTitleFromUrlSlugOffline() {
        val url = "https://example.com/guide/how-to-build-android-apps"
        val title = SmartCategorizer.generateSmartTitle(url, "example.com")
        assertEquals("How To Build Android Apps • example.com", title)
    }

    @Test
    fun testGenerateSmartDescription() {
        val inference = SmartCategorizer.inferCategory("https://youtube.com/watch?v=123", "youtube.com")
        val desc = SmartCategorizer.generateSmartDescription("https://youtube.com/watch?v=123", "youtube.com", inference)
        assertTrue(desc.contains("youtube.com"))
    }

    @Test
    fun testThreadsSmartTitleAndDescription() {
        val url = "https://www.threads.net/@openai/post/DW7RXR7EnRC"
        val inference = SmartCategorizer.inferCategory(url, "threads.net")
        assertEquals("Redes Sociales", inference.name)

        val title = SmartCategorizer.generateSmartTitle(url, "threads.net")
        assertEquals("Post de @openai • Threads", title)

        val descWithSharedText = SmartCategorizer.generateSmartDescription(
            url = url,
            domain = "threads.net",
            inference = inference,
            rawSharedText = "Nuevo modelo lanzado hoy https://www.threads.net/@openai/post/DW7RXR7EnRC"
        )
        assertEquals("Nuevo modelo lanzado hoy", descWithSharedText)
    }
}
