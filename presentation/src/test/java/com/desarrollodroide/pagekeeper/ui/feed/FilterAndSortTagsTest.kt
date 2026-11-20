package com.desarrollodroide.pagekeeper.ui.feed

import com.desarrollodroide.model.Tag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FilterAndSortTagsTest {

    private fun tag(name: String, count: Int) = Tag(id = name.hashCode(), name = name, selected = false, nBookmarks = count)

    private val tags = listOf(
        tag("kotlin", 91),
        tag("android", 214),
        tag("Design", 167),
        tag("golang", 98),
        tag("css", 41),
    )

    private fun names(result: List<Tag>) = result.map { it.name }

    @Test
    fun `most used puts the busiest tags first`() {
        val result = filterAndSortTags(tags, emptySet(), query = "", sortByName = false)
        assertEquals(listOf("android", "Design", "golang", "kotlin", "css"), names(result))
    }

    @Test
    fun `a to z ignores case`() {
        val result = filterAndSortTags(tags, emptySet(), query = "", sortByName = true)
        assertEquals(listOf("android", "css", "Design", "golang", "kotlin"), names(result))
    }

    @Test
    fun `applied tags come first whatever the sort`() {
        val result = filterAndSortTags(tags, setOf("css", "kotlin"), query = "", sortByName = false)
        assertEquals(listOf("kotlin", "css", "android", "Design", "golang"), names(result))
    }

    @Test
    fun `a search matches anywhere in the name, ignoring case`() {
        val result = filterAndSortTags(tags, emptySet(), query = "  O ", sortByName = true)
        assertEquals(listOf("android", "golang", "kotlin"), names(result))
    }

    @Test
    fun `a search never hides an applied tag`() {
        val result = filterAndSortTags(tags, setOf("css"), query = "go", sortByName = true)
        assertEquals(listOf("css", "golang"), names(result))
    }
}
