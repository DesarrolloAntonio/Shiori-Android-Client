package com.desarrollodroide.data.local.room.converters

import com.desarrollodroide.model.Tag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagsConverterTest {

    private val converter = TagsConverter()

    @Test
    fun `fromTagsList converts list of tags to JSON string correctly`() {
        val tags = listOf(
            Tag(id = 1, name = "Tech", selected = true, nBookmarks = 10),
            Tag(id = 2, name = "Science", selected = false, nBookmarks = 5)
        )
        val jsonResult = converter.fromTagsList(tags)
        assertTrue(jsonResult.contains("Tech") && jsonResult.contains("Science"))
    }

    @Test
    fun `toTagsList converts JSON string to list of tags correctly`() {
        val json = """
            [
                {"id":1,"name":"Tech"},
                {"id":2,"name":"Science"}
            ]
        """.trimIndent()
        val tagsList = converter.toTagsList(json)
        assertEquals(2, tagsList.size)
        assertEquals("Tech", tagsList[0].name)
        assertEquals("Science", tagsList[1].name)
    }

    @Test
    fun `toTagsList returns empty list on malformed JSON`() {
        val malformedJson = "this is not a valid json"
        val result = converter.toTagsList(malformedJson)
        assertTrue(result.isEmpty())
    }
}
