package com.damianrdev.save

import com.damianrdev.save.core.common.UrlSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UrlSanitizerTest {

    @Test
    fun testExtractUrlFromMessyInstagramText() {
        val sharedText = "Mira este reel increíble en Instagram: https://www.instagram.com/reel/C8xyz123/?igsh=MWx123abc456 ¡Está buenísimo!"
        val extracted = UrlSanitizer.extractUrl(sharedText)
        assertNotNull(extracted)
        assertEquals("https://www.instagram.com/reel/C8xyz123/?igsh=MWx123abc456", extracted)
    }

    @Test
    fun testExtractUrlFromYouTubeShare() {
        val sharedText = "Kotlin 2.0 en 10 minutos https://youtu.be/abcXYZ123?si=TrackingCode99"
        val extracted = UrlSanitizer.extractUrl(sharedText)
        assertNotNull(extracted)
        assertEquals("https://youtu.be/abcXYZ123?si=TrackingCode99", extracted)
    }

    @Test
    fun testExtractUrlWhenNoUrlPresent() {
        val sharedText = "Texto simple sin ningún enlace"
        val extracted = UrlSanitizer.extractUrl(sharedText)
        assertNull(extracted)
    }

    @Test
    fun testCleanTrackingParameters() {
        val dirtyUrl = "https://www.nytimes.com/article/tech?utm_source=twitter&utm_medium=social&utm_campaign=winter&si=xyz&real_param=123"
        val cleaned = UrlSanitizer.cleanTrackingParameters(dirtyUrl)
        assertEquals("https://www.nytimes.com/article/tech?real_param=123", cleaned)
    }

    @Test
    fun testExtractDomain() {
        val url = "https://www.substack.com/@author/post/123"
        val domain = UrlSanitizer.extractDomain(url)
        assertEquals("substack.com", domain)
    }
}
