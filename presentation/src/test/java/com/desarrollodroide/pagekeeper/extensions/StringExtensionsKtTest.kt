package com.desarrollodroide.pagekeeper.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StringExtensionsKtTest {
    @Test
    fun `isRTLText should return true for Arabic text`() {
        val arabicText = "هذا نص عربي يحتوي على أكثر من مئة حرف عربي. هذا نص عربي يحتوي على أكثر من مئة حرف عربي."
        assertTrue(arabicText.isRTLText())
    }

    @Test
    fun `isRTLText should return false for non-Arabic text`() {
        val nonArabicText = "This is a long enough text with more than one hundred characters to test non-Arabic text. This is a long enough text with more than one hundred characters to test non-Arabic text."
        assertFalse(nonArabicText.isRTLText())
    }

    @Test
    fun `isRTLText should return true for mixed text with more than half Arabic characters`() {
        val mixedText = "هذا نص عربي مع المزيد من النص العربي لضمان أن يحتوي على أكثر من مئة حرف. Some English text included."
        assertTrue(mixedText.isRTLText())
    }


    @Test
    fun `isRTLText should return false for mixed text with less than half Arabic characters`() {
        val mixedText = "This is some English text mixed with عربي قليل to ensure the length is more than one hundred characters."
        assertFalse(mixedText.isRTLText())
    }

    @Test
    fun `isRTLText should work with empty string`() {
        val emptyString = ""
        assertFalse(emptyString.isRTLText())
    }

    /**
     * Cards showed the server's UTC time as if it were local: a bookmark edited at 13:20 in Madrid
     * read 11:20, and from 22:00 to midnight the date was the day before. The web shows the date.
     */
    @Test
    fun `a late evening bookmark shows the device's date, not the UTC one`() {
        val date = "2026-09-14 22:30:00".asLocalBookmarkDate(java.time.ZoneId.of("Europe/Madrid"), java.util.Locale.US)

        assertEquals("Sep 15, 2026", date)
    }

    /** The pair (R7): something that is not a server time is shown as it came, not dropped. */
    @Test
    fun `a value that is not a server time is shown as it came`() {
        assertEquals("yesterday", "yesterday".asLocalBookmarkDate(java.time.ZoneId.of("Europe/Madrid"), java.util.Locale.US))
    }
}
