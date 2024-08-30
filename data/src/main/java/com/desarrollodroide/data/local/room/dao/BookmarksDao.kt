package com.desarrollodroide.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.BookmarkTagCrossRef
import com.desarrollodroide.data.local.room.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarksDao {

  @Query("SELECT * FROM bookmarks")
  fun getAll(): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(bookmarks: List<BookmarkEntity>)

  @Query("DELETE FROM bookmarks")
  suspend fun deleteAll()

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

  @Query("""
    SELECT * FROM bookmarks
    WHERE title LIKE '%' || :searchText || '%'
    ORDER BY id DESC
""")
  fun getPagingBookmarksWithoutTags(searchText: String): PagingSource<Int, BookmarkEntity>

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

  @Query("""
    SELECT * FROM bookmarks
    ORDER BY id DESC
""")
  fun getAllPagingBookmarks(): PagingSource<Int, BookmarkEntity>


  @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
  suspend fun deleteBookmarkById(bookmarkId: Int): Int

  @Query("SELECT (SELECT COUNT(*) FROM bookmarks) == 0")
  suspend fun isEmpty(): Boolean

  @Transaction
  suspend fun insertAllWithTags(bookmarks: List<BookmarkEntity>, tags: List<TagEntity>) {
    insertAll(bookmarks)
    bookmarks.forEach { bookmark ->
      val crossRefs = tags.map { tag ->
        BookmarkTagCrossRef(bookmarkId = bookmark.id, tagId = tag.id)
      }
      insertBookmarkTagCrossRefs(crossRefs)
    }
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmarkTagCrossRefs(crossRefs: List<BookmarkTagCrossRef>)

}