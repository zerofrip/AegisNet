package com.aegisnet.filter.http

import com.aegisnet.filter.http.parser.AdGuardHttpParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HttpRuleEngineTest {

    @Test
    fun `test single domain rule blocking`() {
        val engine = HttpRuleEngine()
        val parser = AdGuardHttpParser()
        
        val rule = parser.parse("||ads.example.com^")
        assertNotNull(rule)
        engine.ingestRules(listOf(rule!!))

        // Match exact domain
        val match1 = engine.match("https://ads.example.com/")
        assertNotNull(match1)
        assertEquals("ads.example.com", match1?.pattern)
        
        // Match subdomain
        val match2 = engine.match("http://sub.ads.example.com/banner.gif")
        assertNotNull(match2)

        // Should not match partial word
        val match3 = engine.match("https://notads.example.com/")
        assertNull(match3)
    }

    @Test
    fun `test path rule blocking`() {
        val engine = HttpRuleEngine()
        val parser = AdGuardHttpParser()
        
        val rule = parser.parse("/banners/ads/")
        assertNotNull(rule)
        engine.ingestRules(listOf(rule!!))

        val match1 = engine.match("https://example.com/banners/ads/1.gif")
        assertNotNull(match1)

        val match2 = engine.match("https://example.com/other/")
        assertNull(match2)
    }
}
