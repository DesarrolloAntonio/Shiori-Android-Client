package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import com.desarrollodroide.data.RememberUserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RememberUserPreferencesSerializerTest {

    private val testRememberUserPreferences = RememberUserPreferences.newBuilder()
        .setId(1)
        .setUsername("userTest")
        .setPassword("passTest")
        .setUrl("https://example.com")
        .setMakeArchivePublic(true)
        .setCreateEbook(false)
        .setCreateArchive(true)
        .build()

    @Test
    fun `test writeTo serializes object correctly`() = runBlocking {
        val testOutputStream = ByteArrayOutputStream()
        RememberUserPreferencesSerializer.writeTo(testRememberUserPreferences, testOutputStream)
        val serializedData = testOutputStream.toByteArray()
        assertTrue(serializedData.isNotEmpty())
    }

    @Test
    fun `test readFrom deserializes object correctly`() = runBlocking {
        val testOutputStream = ByteArrayOutputStream()
        RememberUserPreferencesSerializer.writeTo(testRememberUserPreferences, testOutputStream)
        val serializedData = testOutputStream.toByteArray()
        val testInputStream = ByteArrayInputStream(serializedData)
        val deserializedObject = RememberUserPreferencesSerializer.readFrom(testInputStream)
        assertEquals(testRememberUserPreferences.id, deserializedObject.id)
        assertEquals(testRememberUserPreferences.username, deserializedObject.username)
        assertEquals(testRememberUserPreferences.password, deserializedObject.password)
        assertEquals(testRememberUserPreferences.url, deserializedObject.url)
        assertEquals(testRememberUserPreferences.makeArchivePublic, deserializedObject.makeArchivePublic)
        assertEquals(testRememberUserPreferences.createEbook, deserializedObject.createEbook)
        assertEquals(testRememberUserPreferences.createArchive, deserializedObject.createArchive)
    }


    @Test
    fun `test readFrom throws CorruptionException on corrupted data`(): Unit = runBlocking {
        val corruptedData = "corruptedData".toByteArray()
        val testInputStream = ByteArrayInputStream(corruptedData)
        assertThrows<CorruptionException> {
            RememberUserPreferencesSerializer.readFrom(testInputStream)
        }
    }
}
