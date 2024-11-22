package com.desarrollodroide.data.local.room

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.TagEntity
import com.desarrollodroide.model.Tag
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
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
        createdAt = "2020-01-02",
        imageURL = "http://example.com/image.png",
        hasContent = true,
        hasArchive = true,
        hasEbook = true,
        tags = listOf(),
        createArchive = true,
        createEbook = true
    )

    private val tag = Tag(id = 1, name = "Test Tag")


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
        val updatedBookmark = bookmark.copy(title = "Updated Title", url = "http://updated.com", modified = "2020-01-03")
        bookmarksDao.insertAll(listOf(updatedBookmark))
        val retrievedBookmarks = bookmarksDao.getAll().first()
        assertTrue(retrievedBookmarks.any {
            it.id == bookmark.id && it.title == "Updated Title" && it.url == "http://updated.com" && it.modified == "2020-01-03"
        })
    }

    @Test
    fun testDeleteBookmarkById() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark))
        val deletedRows = bookmarksDao.deleteBookmarkById(1)
        assertEquals(1, deletedRows)
        assertTrue(bookmarksDao.getAll().first().isEmpty())
    }

    @Test
    fun testIsEmpty() = runBlocking {
        assertTrue(bookmarksDao.isEmpty())
        bookmarksDao.insertAll(listOf(bookmark))
        assertFalse(bookmarksDao.isEmpty())
    }

    @Test
    fun testGetPagingBookmarksWithoutTags() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark))
        val pagingSource = bookmarksDao.getPagingBookmarksWithoutTags("Test")
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )
        assertTrue(loadResult is PagingSource.LoadResult.Page)
        assertEquals(1, (loadResult as PagingSource.LoadResult.Page).data.size)
    }

    @Test
    fun testInsertAllWithTags() = runBlocking {
        val bookmarkWithTag = bookmark.copy(tags = listOf(tag))
        bookmarksDao.insertAllWithTags(listOf(bookmarkWithTag))
        val retrievedBookmarks = bookmarksDao.getAll().first()
        assertEquals(1, retrievedBookmarks.size)
        assertEquals(1, retrievedBookmarks[0].tags.size)
        assertEquals("Test Tag", retrievedBookmarks[0].tags[0].name)
    }

    @Test
    fun testUpdateBookmarkWithTags(): Unit = runBlocking {
        // Insert the initial bookmark
        bookmarksDao.insertAllWithTags(listOf(bookmark))

        // Create an updated version of the bookmark with changed fields
        val updatedTag = Tag(id = 2, name = "Updated Tag")
        val updatedBookmark = bookmark.copy(
            title = "Updated Title",
            url = "http://updated-example.com",
            excerpt = "This is an updated test bookmark",
            author = "Updated Author Name",
            isPublic = 0,
            modified = "2023-01-01",
            createdAt = "2023-01-02",
            imageURL = "http://updated-example.com/image.png",
            hasContent = false,
            hasArchive = false,
            hasEbook = false,
            tags = listOf(updatedTag),
            createArchive = false,
            createEbook = false
        )

        // Update the bookmark
        bookmarksDao.updateBookmarkWithTags(updatedBookmark)

        // Retrieve the updated bookmark
        val retrievedBookmark = bookmarksDao.getBookmarkById(1)

        // Assert that the bookmark is not null
        assertNotNull(retrievedBookmark)

        // Check all fields of the updated bookmark
        retrievedBookmark?.let { bookmark ->
            assertEquals(1, bookmark.id)
            assertEquals("Updated Title", bookmark.title)
            assertEquals("http://updated-example.com", bookmark.url)
            assertEquals("This is an updated test bookmark", bookmark.excerpt)
            assertEquals("Updated Author Name", bookmark.author)
            assertEquals(0, bookmark.isPublic)
            assertEquals("2023-01-01", bookmark.modified)
            assertEquals("2023-01-02", bookmark.createdAt)
            assertEquals("http://updated-example.com/image.png", bookmark.imageURL)
            assertFalse(bookmark.hasContent)
            assertFalse(bookmark.hasArchive)
            assertFalse(bookmark.hasEbook)
            assertFalse(bookmark.createArchive)
            assertFalse(bookmark.createEbook)

            // Check the updated tag
            assertEquals(1, bookmark.tags.size)
            assertEquals(2, bookmark.tags[0].id)
        }
    }

    @Test
    fun testGetAllBookmarkIds() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark, bookmark.copy(id = 2)))
        val bookmarkIds = bookmarksDao.getAllBookmarkIds()
        assertEquals(listOf(1, 2), bookmarkIds)
    }

    @Test
    fun testGetBookmarkById() = runBlocking {
        bookmarksDao.insertAll(listOf(bookmark))
        val retrievedBookmark = bookmarksDao.getBookmarkById(1)
        assertNotNull(retrievedBookmark)
        assertEquals(bookmark, retrievedBookmark)
    }

}
