package com.desarrollodroide.data.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IntExtensionsTest {

    @Test
    fun `isTimestampId returns true for timestamp-based ids`() {
        // Given - a value generated from System.currentTimeMillis() / 1000
        val timestampId = 1739000000

        // When
        val result = timestampId.isTimestampId()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isTimestampId returns true for values just above threshold`() {
        // Given
        val id = 1_000_001

        // When
        val result = id.isTimestampId()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isTimestampId returns false for regular server ids`() {
        // Given
        val regularId = 1

        // When
        val result = regularId.isTimestampId()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isTimestampId returns false for threshold value`() {
        // Given
        val thresholdId = 1_000_000

        // When
        val result = thresholdId.isTimestampId()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isTimestampId returns false for large server ids`() {
        // Given - even a server with many bookmarks
        val largeServerId = 999_999

        // When
        val result = largeServerId.isTimestampId()

        // Then
        assertFalse(result)
    }
}