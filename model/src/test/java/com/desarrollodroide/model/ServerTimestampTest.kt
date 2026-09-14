package com.desarrollodroide.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The server keeps bookmark times in UTC, and the app wrote its own in the device's local time into
 * the same column: an edit made at 13:20 in Madrid was stored as 13:20 next to server times two
 * hours behind it, so no single conversion could show both right.
 */
class ServerTimestampTest {

    @Test
    fun `the app writes now in UTC whatever the device's zone`() {
        val madrid = Clock.fixed(Instant.parse("2026-09-14T11:20:49Z"), ZoneId.of("Europe/Madrid"))

        assertEquals("2026-09-14 11:20:49", serverTimestampNow(madrid))
    }

    /** The pair (R7): on a device already in UTC nothing moves. */
    @Test
    fun `a device in UTC writes the same instant`() {
        val utc = Clock.fixed(Instant.parse("2026-09-14T23:59:59Z"), ZoneId.of("UTC"))

        assertEquals("2026-09-14 23:59:59", serverTimestampNow(utc))
    }
}
