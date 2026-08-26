package com.desarrollodroide.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.BookmarkHtmlEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
class BookmarkHtmlDaoTest {

    private lateinit var database: BookmarksDatabase
    private lateinit var bookmarkHtmlDao: BookmarkHtmlDao

    private val bookmarkHtml = BookmarkHtmlEntity(
        id = 1,
        url = "http://example.com",
        readableContentHtml = "<html>Test Content</html>"
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BookmarksDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        bookmarkHtmlDao = database.bookmarkHtmlDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndFetchBookmarkHtml(): Unit = runBlocking {
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml)
        val retrievedHtml = bookmarkHtmlDao.getHtmlContent(bookmarkHtml.id)
        Assert.assertEquals(bookmarkHtml.readableContentHtml, retrievedHtml)
        bookmarkHtmlDao.getBookmarkHtml(bookmarkHtml.id)?.let {
            Assert.assertEquals(bookmarkHtml, it)
        }
    }

    /**
     * Logout has to take the cached article text with it. Bookmark ids restart per server, so a
     * row left behind can be handed to whoever signs in next: the offline fallback looks content
     * up by id alone, with nothing tying it to an account.
     */
    @Test
    fun deleteAllClearsCachedArticles() = runBlocking {
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml)

        bookmarkHtmlDao.deleteAll()

        Assert.assertNull(bookmarkHtmlDao.getHtmlContent(bookmarkHtml.id))
    }

    /**
     * Nothing else ever removed a row, so the table only grew: deleting a bookmark left its full
     * article text behind for good.
     */
    @Test
    fun orphanedHtmlIsPrunedWhenItsBookmarkIsGone() = runBlocking {
        val bookmarksDao = database.bookmarksDao()
        bookmarksDao.insertAll(
            listOf(
                BookmarkEntity(
                    id = 1,
                    url = "http://example.com",
                    title = "Still here",
                    excerpt = "",
                    author = "",
                    isPublic = 0,
                    modified = "2020-01-01",
                    createdAt = "2020-01-01",
                    imageURL = "",
                    hasContent = true,
                    hasArchive = false,
                    hasEbook = false,
                    tags = listOf(),
                    createArchive = false,
                    createEbook = false
                )
            )
        )
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml)
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml.copy(id = 2, url = "http://deleted.example"))

        bookmarkHtmlDao.deleteOrphanedHtml()

        Assert.assertNotNull(bookmarkHtmlDao.getHtmlContent(1))
        Assert.assertNull(bookmarkHtmlDao.getHtmlContent(2))
    }

    @Test
    fun testUpdateBookmarkHtml() = runBlocking {
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml)
        val updatedBookmarkHtml = bookmarkHtml.copy(readableContentHtml = "<html>Updated Content</html>")
        bookmarkHtmlDao.insertOrUpdate(updatedBookmarkHtml)
        val retrievedHtml = bookmarkHtmlDao.getHtmlContent(bookmarkHtml.id)
        Assert.assertEquals(updatedBookmarkHtml.readableContentHtml, retrievedHtml)
    }
}