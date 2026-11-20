package com.desarrollodroide.pagekeeper.ui.tags

import com.desarrollodroide.model.Tag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagListEntriesTest {

    private fun tag(name: String, count: Int = 1) = Tag(id = name.hashCode(), name = name, selected = false, nBookmarks = count)

    private fun describe(entries: List<TagListEntry>) = entries.map {
        when (it) {
            is TagListEntry.Letter -> "[${it.letter}]"
            is TagListEntry.Row -> buildString {
                append(it.tag.name)
                if (it.first) append("^")
                if (it.last) append("$")
            }
        }
    }

    @Test
    fun `most used is one group with no headings`() {
        val entries = tagListEntries(listOf(tag("android"), tag("css"), tag("go")), sortByName = false)
        assertEquals(listOf("android^", "css", "go$"), describe(entries))
    }

    @Test
    fun `a to z puts each initial under its own heading, case folded`() {
        val entries = tagListEntries(listOf(tag("android"), tag("Archiving"), tag("css")), sortByName = true)
        assertEquals(listOf("[A]", "android^", "Archiving$", "[C]", "css^$"), describe(entries))
    }

    @Test
    fun `anything not starting with a letter goes under one hash heading`() {
        val entries = tagListEntries(listOf(tag("2024"), tag("_inbox"), tag("android")), sortByName = true)
        assertEquals(listOf("[#]", "2024^", "_inbox$", "[A]", "android^$"), describe(entries))
    }

    @Test
    fun `keys are unique so the lazy list does not crash`() {
        val entries = tagListEntries(listOf(tag("a"), tag("b"), tag("c")), sortByName = true)
        assertEquals(entries.size, entries.map { it.key }.toSet().size)
    }

    @Test
    fun `usage never divides by zero and a used tag is never invisible`() {
        assertEquals(0f, usageOf(0, 0))
        assertEquals(0f, usageOf(0, 214))
        assertEquals(1f, usageOf(214, 214))
        assertTrue(usageOf(1, 10_000) >= 0.04f)
    }
}
