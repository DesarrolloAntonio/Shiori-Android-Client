package com.desarrollodroide.data.local.room

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookmarksDaoTest {

    private lateinit var database: BookmarksDatabase
    private lateinit var bookmarksDao: BookmarksDao
    private val bookmark = BookmarkEntity(
        id = 1,
        url = "http://example.com",
        title = "Test Bookmark",
        excerpt = "This is a test bookmark",
        author = "Author Name",
        isPublic = 1,
        modified = "2020-01-01",
        imageURL = "http://example.com/image.png",
        hasContent = true,
        hasArchive = true,
        hasEbook = true,
        tags = listOf(),
        createArchive = true,
        createEbook = true
    )


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            BookmarksDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        bookmarksDao = database.bookmarksDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndFetchBookmarks() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark))
        val retrievedBookmarks = bookmarksDao.getAll().first()
        assertTrue(retrievedBookmarks.contains(bookmark))
        bookmarksDao.deleteAll()
        assertTrue(bookmarksDao.getAll().first().isEmpty())
    }

    @Test
    fun testUpdateBookmark() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark))
        val updatedBookmark = bookmark.copy(title = "Updated Title", url = "http://updated.com")
        bookmarksDao.insertAll(listOf(updatedBookmark))
        val retrievedBookmarks = bookmarksDao.getAll().first()
        assertTrue(retrievedBookmarks.any {
            it.id == bookmark.id && it.title == "Updated Title" && it.url == "http://updated.com"
        })
    }

}
