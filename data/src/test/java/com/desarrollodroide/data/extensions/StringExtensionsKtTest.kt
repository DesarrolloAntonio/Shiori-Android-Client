package com.desarrollodroide.data.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class StringExtensionsTest {

    @Test
    fun `removeTrailingSlash removes trailing slash if present`() {
        // Given a string with a trailing slash
        val stringWithSlash = "https://www.example.com/"

        // When removeTrailingSlash is called
        val result = stringWithSlash.removeTrailingSlash()

        // Then the result should not have a trailing slash
        assertEquals("https://www.example.com", result)
    }

    @Test
    fun `removeTrailingSlash does nothing if no trailing slash present`() {
        // Given a string without a trailing slash
        val stringWithoutSlash = "https://www.example.com"

        // When removeTrailingSlash is called
        val result = stringWithoutSlash.removeTrailingSlash()

        // Then the result should be the same as the input
        assertEquals(stringWithoutSlash, result)
    }

    @Test
    fun `removeTrailingSlash works with empty string`() {
        // Given an empty string
        val emptyString = ""

        // When removeTrailingSlash is called
        val result = emptyString.removeTrailingSlash()

        // Then the result should still be an empty string
        assertEquals("", result)
    }

    @Test
    fun `removeTrailingSlash does nothing to string without any slash`() {
        // Given a string without any slashes
        val stringWithoutAnySlash = "www.example.com"

        // When removeTrailingSlash is called
        val result = stringWithoutAnySlash.removeTrailingSlash()

        // Then the result should be the same as the input
        assertEquals(stringWithoutAnySlash, result)
    }

}
