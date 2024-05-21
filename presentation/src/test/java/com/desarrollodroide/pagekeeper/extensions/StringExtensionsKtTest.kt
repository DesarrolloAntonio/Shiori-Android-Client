package com.desarrollodroide.pagekeeper.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StringExtensionsKtTest {
    @Test
    fun `isRTLText should return true for Arabic text`() {
        val arabicText = "گروه هافلپاف"
        assertTrue(arabicText.isRTLText())
    }

    @Test
    fun `isRTLText should return false for non-Arabic text`() {
        val nonArabicText = "Hello, World!"
        assertFalse(nonArabicText.isRTLText())
    }

    @Test
    fun `isRTLText should return true for mixed text with more than half Arabic characters`() {
        val mixedText = "هذا نص عربي مع some English"
        assertTrue(mixedText.isRTLText())
    }

    @Test
    fun `isRTLText should return false for mixed text with less than half Arabic characters`() {
        val mixedText = "This is some English with عربي قليل"
        assertFalse(mixedText.isRTLText())
    }

    @Test
    fun `isRTLText should work with empty string`() {
        val emptyString = ""
        assertFalse(emptyString.isRTLText())
    }
}