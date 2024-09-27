package com.desarrollodroide.data.extensions

import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BookmarkExtensionTest {

    @Test
    fun `toBodyJson correctly serializes bookmark to JSON with custom tag serialization`() {
        // Given a Bookmark instance with predefined values
        val tags = listOf(Tag(id = 1, "education"), Tag(id = 2,"reading"))
        val bookmark = Bookmark(
            id = 1,
            url = "https://example.com",
            title = "Example Title",
            excerpt = "An example excerpt",
            author = "Author Name",
            public = 1,
            modified = "2023-01-01T12:00:00",
            imageURL = "https://example.com/image.jpg",
            hasContent = true,
            hasArchive = false,
            hasEbook = false,
            tags = tags,
            createArchive = true,
            createEbook = false
        )

        // When converting Bookmark to JSON string with custom Tag serialization
        val json = bookmark.toBodyJson()

        // Then the JSON should contain the correct attributes and values, including custom tag serialization
        assertTrue(json.contains("\"id\":1"))
        assertTrue(json.contains("\"url\":\"https://example.com\""))
        assertTrue(json.contains("\"title\":\"Example Title\""))
        assertTrue(json.contains("\"excerpt\":\"An example excerpt\""))
        assertTrue(json.contains("\"author\":\"Author Name\""))
        assertTrue(json.contains("\"public\":1"))
        assertTrue(json.contains("\"modified\":\"2023-01-01T12:00:00\""))
        assertTrue(json.contains("\"imageURL\":\"https://example.com/image.jpg\""))
        assertTrue(json.contains("\"hasContent\":true"))
        assertTrue(json.contains("\"hasArchive\":false"))
        assertTrue(json.contains("\"hasEbook\":false"))
        assertTrue(json.contains("\"createArchive\":true"))
        assertTrue(json.contains("\"createEbook\":false"))
        assertTrue(json.contains("\"tags\":[{\"name\":\"education\"},{\"name\":\"reading\"}]")) // Verifying custom Tag serialization
    }
}
