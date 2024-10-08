package com.desarrollodroide.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.BookmarkTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarksDao {

  // Basic CRUD operations

  /**
   * Retrieves all bookmarks from the database.
   * @return A Flow of List<BookmarkEntity> representing all bookmarks.
   */
  @Query("SELECT * FROM bookmarks")
  fun getAll(): Flow<List<BookmarkEntity>>

  /**
   * Inserts a list of bookmarks into the database, replacing any existing entries with the same IDs.
   * @param bookmarks The list of BookmarkEntity objects to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(bookmarks: List<BookmarkEntity>)

  /**
   * Deletes all bookmarks from the database.
   */
  @Query("DELETE FROM bookmarks")
  suspend fun deleteAll()

  /**
   * Deletes a specific bookmark by its ID.
   * @param bookmarkId The ID of the bookmark to delete.
   * @return The number of rows affected (should be 1 if successful, 0 if the bookmark was not found).
   */
  @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
  suspend fun deleteBookmarkById(bookmarkId: Int): Int

  /**
   * Checks if the bookmarks table is empty.
   * @return true if the table is empty, false otherwise.
   */
  @Query("SELECT (SELECT COUNT(*) FROM bookmarks) == 0")
  suspend fun isEmpty(): Boolean

  // Paging operations

  /**
   * Retrieves bookmarks for paging, filtered by search text and tags.
   * @param searchText The text to search for in bookmark titles.
   * @param tagIds The list of tag IDs to filter by.
   * @return A PagingSource of BookmarkEntity objects.
   */
  @Query("""
        SELECT * FROM bookmarks
        WHERE (:searchText = '' OR title LIKE '%' || :searchText || '%')
        AND EXISTS (
            SELECT 1 FROM bookmark_tag_cross_ref 
            WHERE bookmark_tag_cross_ref.bookmarkId = bookmarks.id
            AND bookmark_tag_cross_ref.tagId IN (:tagIds)
        )
        ORDER BY id DESC
    """)
  fun getPagingBookmarks(
    searchText: String,
    tagIds: List<Int>
  ): PagingSource<Int, BookmarkEntity>

  /**
   * Retrieves bookmarks for paging, filtered by search text without considering tags.
   * @param searchText The text to search for in bookmark titles.
   * @return A PagingSource of BookmarkEntity objects.
   */
  @Query("""
        SELECT * FROM bookmarks
        WHERE title LIKE '%' || :searchText || '%'
        ORDER BY id DESC
    """)
  fun getPagingBookmarksWithoutTags(searchText: String): PagingSource<Int, BookmarkEntity>

  /**
   * Retrieves bookmarks for paging, filtered by tags.
   * @param tagIds The list of tag IDs to filter by.
   * @return A PagingSource of BookmarkEntity objects.
   */
  @Query("""
        SELECT * FROM bookmarks
        WHERE EXISTS (
            SELECT 1 FROM bookmark_tag_cross_ref 
            WHERE bookmark_tag_cross_ref.bookmarkId = bookmarks.id
            AND bookmark_tag_cross_ref.tagId IN (:tagIds)
        )
        ORDER BY id DESC
    """)
  fun getPagingBookmarksByTags(tagIds: List<Int>): PagingSource<Int, BookmarkEntity>

  /**
   * Retrieves all bookmarks for paging without any filters.
   * @return A PagingSource of BookmarkEntity objects.
   */
  @Query("""
        SELECT * FROM bookmarks
        ORDER BY id DESC
    """)
  fun getAllPagingBookmarks(): PagingSource<Int, BookmarkEntity>

  // Tag-related operations

  /**
   * Inserts bookmark-tag cross references into the database.
   * @param crossRefs The list of BookmarkTagCrossRef objects to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmarkTagCrossRefs(crossRefs: List<BookmarkTagCrossRef>)

  /**
   * Clears all bookmark-tag cross references from the database.
   */
  @Query("DELETE FROM bookmark_tag_cross_ref")
  suspend fun clearBookmarkTagCrossRefs()

  /**
   * Inserts a list of bookmarks along with their associated tags.
   * This method performs the following steps in a single transaction:
   * 1. Clears existing bookmark-tag cross references
   * 2. Deletes all existing bookmarks
   * 3. Inserts the new bookmarks
   * 4. Creates new bookmark-tag cross references for bookmarks with tags
   *
   * @param bookmarks The list of BookmarkEntity objects to insert, including their tags.
   */
  @Transaction
  suspend fun insertAllWithTags(bookmarks: List<BookmarkEntity>) {
    clearBookmarkTagCrossRefs()
    deleteAll()
    insertAll(bookmarks)
    val bookmarksWithTags = bookmarks.filter { it.tags.isNotEmpty() }
    bookmarksWithTags.forEach { bookmark ->
      val crossRefs = bookmark.tags.map { tag ->
        BookmarkTagCrossRef(bookmarkId = bookmark.id, tagId = tag.id)
      }
      insertBookmarkTagCrossRefs(crossRefs)
    }
  }

  /**
   * Updates an existing bookmark in the local database.
   *
   * This method uses Room's @Update annotation, which generates the necessary SQL
   * to update the bookmark based on its primary key. If the bookmark doesn't exist
   * in the database, no action will be taken.
   *
   * @param bookmark The BookmarkEntity to be updated in the database.
   *                 It must have a valid ID that matches an existing entry.
   */
  @Update
  suspend fun updateBookmark(bookmark: BookmarkEntity)

  /**
   * Retrieves a list of all bookmark IDs from the local database.
   * This can be useful for performing operations on all bookmarks, such as
   * deleting or updating them.
   *
   * @return A list of all bookmark IDs in the database.
   */
  @Query("SELECT id FROM bookmarks")
  suspend fun getAllBookmarkIds(): List<Int>

  /**
   * Retrieves a bookmark by its ID.
   * This can be useful to determine if a bookmark already exists in the database
   * and if its version is outdated.
   *
   * @param bookmarkId The ID of the bookmark to retrieve.
   * @return The BookmarkEntity if found, or null otherwise.
   */
  @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
  suspend fun getBookmarkById(bookmarkId: Int): BookmarkEntity?

  /**
   * Updates an existing bookmark in the local database, including its associated tags.
   *
   * This method performs the following steps in a single transaction:
   * 1. Updates the bookmark entity
   * 2. Deletes all existing tag associations for the bookmark
   * 3. Inserts new tag associations for the bookmark
   *
   * @param bookmark The BookmarkEntity to be updated in the database.
   *                 It must have a valid ID that matches an existing entry.
   */
  @Transaction
  suspend fun updateBookmarkWithTags(bookmark: BookmarkEntity) {
    updateBookmark(bookmark)
    deleteBookmarkTagCrossRefs(bookmark.id)
    val newCrossRefs = bookmark.tags.map { tag ->
      BookmarkTagCrossRef(bookmarkId = bookmark.id, tagId = tag.id)
    }
    insertBookmarkTagCrossRefs(newCrossRefs)
  }

  /**
   * Deletes all bookmark-tag cross references associated with a bookmark.
   *
   * @param bookmarkId The ID of the bookmark to delete associated tags for.
   */
  @Query("DELETE FROM bookmark_tag_cross_ref WHERE bookmarkId = :bookmarkId")
  suspend fun deleteBookmarkTagCrossRefs(bookmarkId: Int)

}