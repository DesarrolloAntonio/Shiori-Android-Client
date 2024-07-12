package com.desarrollodroide.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import com.desarrollodroide.data.local.room.entity.TagEntity
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class TagDaoTest {

    private lateinit var database: BookmarksDatabase
    private lateinit var tagDao: TagDao

    private val tag = TagEntity(
        id = 1,
        name = "Test Tag",
        nBookmarks = 5
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookmarksDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        tagDao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndFetchTags() = runBlocking {
        tagDao.insertTag(tag)
        val retrievedTags = tagDao.getAllTags().first()
        assertTrue(retrievedTags.contains(tag))
        tagDao.deleteAllTags()
        assertTrue(tagDao.getAllTags().first().isEmpty())
    }

    @Test
    fun testDeleteTag() = runBlocking {
        tagDao.insertTag(tag)
        tagDao.deleteTag(tag)
        val retrievedTags = tagDao.getAllTags().first()
        assertFalse(retrievedTags.contains(tag))
    }

    @Test
    fun testInsertAndFetchMultipleTags() = runBlocking {
        val tags = listOf(
            TagEntity(1, "Tag1", 2),
            TagEntity(2, "Tag2", 3)
        )
        tagDao.insertAllTags(tags)
        val retrievedTags = tagDao.getAllTags().first()
        assertTrue(retrievedTags.containsAll(tags))
    }
}
