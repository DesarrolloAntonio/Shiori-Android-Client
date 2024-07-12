package com.desarrollodroide.data.local.datastore

import androidx.datastore.core.CorruptionException
import com.desarrollodroide.data.HideTag
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HideTagSerializerTest {

    private val testHideTag = HideTag.newBuilder()
        .setId(1)
        .setName("TestTag")
        .build()

    @Test
    fun `test writeTo serializes object correctly`() = runBlocking {
        val testOutputStream = ByteArrayOutputStream()
        HideTagSerializer.writeTo(testHideTag, testOutputStream)
        val serializedData = testOutputStream.toByteArray()
        assertTrue(serializedData.isNotEmpty())
    }

    @Test
    fun `test readFrom deserializes object correctly`() = runBlocking {
        val testOutputStream = ByteArrayOutputStream()
        HideTagSerializer.writeTo(testHideTag, testOutputStream)
        val serializedData = testOutputStream.toByteArray()
        val testInputStream = ByteArrayInputStream(serializedData)
        val deserializedObject = HideTagSerializer.readFrom(testInputStream)
        assertEquals(testHideTag.id, deserializedObject.id)
        assertEquals(testHideTag.name, deserializedObject.name)
    }

    @Test
    fun `test readFrom throws CorruptionException on corrupted data`(): Unit = runBlocking {
        val corruptedData = "corruptedData".toByteArray()
        val testInputStream = ByteArrayInputStream(corruptedData)
        assertThrows<CorruptionException> {
            HideTagSerializer.readFrom(testInputStream)
        }
    }
}