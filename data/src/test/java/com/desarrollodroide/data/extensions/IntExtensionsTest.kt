package com.desarrollodroide.data.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IntExtensionsTest {

    @Test
    fun `isTimestampId returns true for values greater than base timestamp`() {
        // Given
        val timestampId = 1704067201  // One second after base timestamp

        // When
        val result = timestampId.isTimestampId()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isTimestampId returns false for values less than base timestamp`() {
        // Given
        val regularId = 1704067199  // One second before base timestamp

        // When
        val result = regularId.isTimestampId()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isTimestampId returns false for small regular ids`() {
        // Given
        val regularId = 1

        // When
        val result = regularId.isTimestampId()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isTimestampId returns false for base timestamp value`() {
        // Given
        val baseTimestamp = 1704067200  // Exactly the base timestamp

        // When
        val result = baseTimestamp.isTimestampId()

        // Then
        assertFalse(result)
    }
}