package com.desarrollodroide.data.local.preferences

import androidx.datastore.core.CorruptionException
import com.desarrollodroide.data.RememberUserPreferences
import com.desarrollodroide.data.local.datastore.RememberUserPreferencesSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class RememberUserPreferencesSerializerTest {

    @Test
    fun `Serializer reads from input stream correctly`() = runTest {
        // Given a valid RememberUserPreferences protobuf
        val originalPreferences = RememberUserPreferences.newBuilder()
            .setId(123)
            .setUsername("testUser")
            .setPassword("testPass")
            .setUrl("https://example.com")
            .setMakeArchivePublic(true)
            .setCreateEbook(false)
            .setCreateArchive(true)
            .build()

        val inputStream = ByteArrayInputStream(originalPreferences.toByteArray())

        // When reading from the input stream
        val resultPreferences = RememberUserPreferencesSerializer.readFrom(inputStream)

        // Then the deserialized object should match the original
        assertEquals(originalPreferences, resultPreferences)
    }

    @Test
    fun `Serializer writes to output stream correctly`() = runTest {
        // Given a valid RememberUserPreferences object
        val preferences = RememberUserPreferences.newBuilder()
            .setId(456)
            .setUsername("anotherUser")
            .setPassword("anotherPass")
            .setUrl("https://another-example.com")
            .setMakeArchivePublic(false)
            .setCreateEbook(true)
            .setCreateArchive(false)
            .build()

        val outputStream = ByteArrayOutputStream()

        // When writing to the output stream
        RememberUserPreferencesSerializer.writeTo(preferences, outputStream)

        // Then the serialized bytes should match the original object's bytes
        assertEquals(preferences, RememberUserPreferences.parseFrom(outputStream.toByteArray()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Serializer throws CorruptionException on invalid input`() = runTest {
        // Given an invalid byte array (corrupted data)
        val corruptedData = byteArrayOf(1, 2, 3, 4)
        val inputStream = ByteArrayInputStream(corruptedData)

        // Then assert that reading from the input stream throws a CorruptionException
        assertFailsWith<CorruptionException> {
            RememberUserPreferencesSerializer.readFrom(inputStream)
        }
    }
}
