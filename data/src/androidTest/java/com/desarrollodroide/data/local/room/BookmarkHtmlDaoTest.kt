package com.desarrollodroide.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.desarrollodroide.data.local.room.dao.BookmarkHtmlDao
import com.desarrollodroide.data.local.room.database.BookmarksDatabase
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

    @Test
    fun testUpdateBookmarkHtml() = runBlocking {
        bookmarkHtmlDao.insertOrUpdate(bookmarkHtml)
        val updatedBookmarkHtml = bookmarkHtml.copy(readableContentHtml = "<html>Updated Content</html>")
        bookmarkHtmlDao.insertOrUpdate(updatedBookmarkHtml)
        val retrievedHtml = bookmarkHtmlDao.getHtmlContent(bookmarkHtml.id)
        Assert.assertEquals(updatedBookmarkHtml.readableContentHtml, retrievedHtml)
    }
}