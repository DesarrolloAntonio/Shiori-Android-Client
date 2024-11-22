package com.desarrollodroide.data.helpers

import com.desarrollodroide.model.Tag
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TagTypeAdapterTest {

    @Test
    fun `secondary constructor initializes properties correctly`() {
        // Given a tag name
        val tagName = "exampleTag"

        // When creating a Tag using the secondary constructor
        val tag = Tag(id = 1, tagName)

        // Then the properties should be set to default values except for the name
        assertEquals(1, tag.id)
        assertEquals(tagName, tag.name)
        assertEquals(false, tag.selected)
        assertEquals(0, tag.nBookmarks)
    }

    @Test
    fun `TagTypeAdapter serializes Tag correctly with all fields`() {
        // Given a Tag object with all fields initialized
        val tag = Tag(1, "exampleTag", true, 5)

        // And a Gson instance with TagTypeAdapter registered
        val gson = GsonBuilder()
            .registerTypeAdapter(Tag::class.java, TagTypeAdapter())
            .create()

        // When serializing the Tag object
        val json = gson.toJson(tag, Tag::class.java)

        // Then the resulting JSON should contain all the necessary properties
        val expectedJson = "{\"name\":\"exampleTag\"}" // Note: Only 'name' is expected as per TagTypeAdapter
        assertEquals(expectedJson, json)
    }

}