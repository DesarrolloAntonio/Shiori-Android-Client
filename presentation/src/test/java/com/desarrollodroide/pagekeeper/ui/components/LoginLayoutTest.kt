package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Where the login branding goes.
 *
 * Stacked, the login screen wants about 680dp of height. A landscape tablet has 540dp to 600dp,
 * so the Log in button sat under the gesture bar — and on a 1080p tablet only its top edge was on
 * screen at all. It scrolled, but nothing on screen said so.
 */
class LoginLayoutTest {

    @Test
    fun `a landscape tablet puts the branding beside the form`() {
        // Pixel Tablet, landscape: 1920x1200 at 320dpi.
        assertTrue(shouldPlaceBrandingBeside(availableWidth = 960.dp, availableHeight = 600.dp))
        // A 1080p tablet, where the button was almost entirely off screen.
        assertTrue(shouldPlaceBrandingBeside(availableWidth = 960.dp, availableHeight = 540.dp))
    }

    @Test
    fun `a landscape phone puts the branding beside the form too`() {
        assertTrue(shouldPlaceBrandingBeside(availableWidth = 800.dp, availableHeight = 360.dp))
    }

    @Test
    fun `a phone in portrait keeps the stacked layout`() {
        assertFalse(shouldPlaceBrandingBeside(availableWidth = 411.dp, availableHeight = 891.dp))
    }

    @Test
    fun `a tablet in portrait keeps the stacked layout`() {
        // Wide enough for two columns, but tall enough that stacked fits, and stacked looks better.
        assertFalse(shouldPlaceBrandingBeside(availableWidth = 800.dp, availableHeight = 1280.dp))
    }

    @Test
    fun `a narrow short window stays stacked rather than showing two cramped columns`() {
        assertFalse(shouldPlaceBrandingBeside(availableWidth = 480.dp, availableHeight = 400.dp))
    }
}
