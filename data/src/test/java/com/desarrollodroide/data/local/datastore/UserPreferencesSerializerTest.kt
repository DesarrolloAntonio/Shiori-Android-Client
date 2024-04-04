package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import com.desarrollodroide.data.UserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UserPreferencesSerializerTest {

    private val testUserPreferences = UserPreferences.newBuilder()
        .setId(1)
        .setUsername("testUser")
        .setPassword("testPass")
        .setOwner(true)
        .setSession("testSession")
        .setUrl("https://test.url")
        .setRememberPassword(true)
        .setIsLegacyApi(false)
        .setToken("testToken")
        .build()

    @Test
    fun `writeTo serializes UserPreferences correctly`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        UserPreferencesSerializer.writeTo(testUserPreferences, outputStream)
        val serializedData = outputStream.toByteArray()
        assertTrue(serializedData.isNotEmpty(), "Serialized data should not be empty")
    }

    @Test
    fun `readFrom deserializes UserPreferences correctly`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        UserPreferencesSerializer.writeTo(testUserPreferences, outputStream)
        val serializedData = outputStream.toByteArray()
        val inputStream = ByteArrayInputStream(serializedData)
        val deserializedPreferences = UserPreferencesSerializer.readFrom(inputStream)
        assertEquals(testUserPreferences, deserializedPreferences, "Deserialized object should match the original")
    }

    @Test
    fun `readFrom throws CorruptionException on corrupted data`(): Unit = runBlocking {
        val corruptedData = "corruptedData".toByteArray()
        val inputStream = ByteArrayInputStream(corruptedData)
        assertThrows<CorruptionException> {
            UserPreferencesSerializer.readFrom(inputStream)
        }
    }
}
