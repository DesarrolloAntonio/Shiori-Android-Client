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

    /**
     * Search has to look past the title. It backs the feed's own field now, so a query only ever
     * matching titles finds nothing for the words a user actually remembers about a page. The web
     * searches the content too; that is not cached locally, so the url is the nearest stand-in.
     */
    @Test
    fun pagingSearchMatchesExcerptAndUrl() = runBlocking {
        bookmarksDao.insertAll(
            listOf(
                bookmark.copy(id = 1, title = "Nothing relevant", excerpt = "", url = "http://a.test"),
                bookmark.copy(id = 2, title = "Nothing relevant", excerpt = "about coroutines", url = "http://b.test"),
                bookmark.copy(id = 3, title = "Nothing relevant", excerpt = "", url = "http://kotlinlang.org"),
            )
        )

        assertEquals(listOf(2), searchIds("coroutines"))
        assertEquals(listOf(3), searchIds("kotlinlang"))
    }

    /**
     * Search and a tag filter at once. This combination only became reachable when the app bar's
     * field started driving the feed: the old search screen always passed an empty tag list, so
     * the two-condition query was never actually exercised in production.
     */
    @Test
    fun pagingSearchCombinesWithATagFilter() = runBlocking {
        bookmarksDao.insertAllWithTags(
            listOf(
                bookmark.copy(id = 1, title = "Tagged and matching", tags = listOf(tag)),
                bookmark.copy(id = 2, title = "Matching but untagged", tags = listOf()),
            )
        )

        val loadResult = bookmarksDao.getPagingBookmarks(
            searchText = "Matching",
            tagIds = listOf(tag.id)
        ).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(loadResult is PagingSource.LoadResult.Page)
        assertEquals(
            listOf(1),
            (loadResult as PagingSource.LoadResult.Page).data.map { it.id }
        )
    }

    /**
     * The reason editing a bookmark used to be followed by a full sync.
     *
     * A plain update writes the bookmark row and nothing else, so bookmark_tag_cross_ref keeps the
     * tags the bookmark had before. Tag filtering reads that table, so the bookmark went on being
     * filed under a tag it no longer has. The full sync afterwards rebuilt the table and hid it.
     */
    @Test
    fun plainUpdateLeavesTagCrossRefsStale() = runBlocking {
        val other = Tag(id = 2, name = "Other Tag")
        bookmarksDao.insertAllWithTags(listOf(bookmark.copy(tags = listOf(tag))))

        bookmarksDao.updateBookmark(bookmark.copy(tags = listOf(other)))

        assertEquals(
            "a plain update does not touch the cross reference table",
            listOf(bookmark.id),
            bookmarkIdsForTag(tag.id)
        )
        assertTrue(bookmarkIdsForTag(other.id).isEmpty())
    }

    @Test
    fun updateWithTagsReplacesTheCrossRefs() = runBlocking {
        val other = Tag(id = 2, name = "Other Tag")
        bookmarksDao.insertAllWithTags(listOf(bookmark.copy(tags = listOf(tag))))

        bookmarksDao.updateBookmarkWithTags(bookmark.copy(tags = listOf(other)))

        assertTrue(
            "the old tag must stop matching once it has been removed",
            bookmarkIdsForTag(tag.id).isEmpty()
        )
        assertEquals(listOf(bookmark.id), bookmarkIdsForTag(other.id))
    }

    private suspend fun bookmarkIdsForTag(tagId: Int): List<Int> {
        val loadResult = bookmarksDao.getPagingBookmarksByTags(listOf(tagId)).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )
        assertTrue(loadResult is PagingSource.LoadResult.Page)
        return (loadResult as PagingSource.LoadResult.Page).data.map { it.id }
    }

    private suspend fun searchIds(query: String): List<Int> {
        val loadResult = bookmarksDao.getPagingBookmarksWithoutTags(query).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )
        assertTrue(loadResult is PagingSource.LoadResult.Page)
        return (loadResult as PagingSource.LoadResult.Page).data.map { it.id }
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
