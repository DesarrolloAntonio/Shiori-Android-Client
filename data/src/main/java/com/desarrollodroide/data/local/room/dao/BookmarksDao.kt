package com.desarrollodroide.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.model.Bookmark
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
        WHERE 
            (:searchText = '' OR title LIKE '%' || :searchText || '%')
        AND 
            (:tagsListSize = 0 OR tags IN (:tags))
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
    """)
  suspend fun getPagingBookmarks(
    searchText: String,
    tags: List<String>,
    tagsListSize: Int,
    limit: Int,
    offset: Int
  ): List<BookmarkEntity>

  @Query("""
        SELECT COUNT(*) FROM bookmarks 
        WHERE 
            (:searchText = '' OR title LIKE '%' || :searchText || '%')
        AND 
            (:tagsListSize = 0 OR tags IN (:tags))
    """)
  suspend fun getPagingBookmarksCount(
    searchText: String,
    tags: List<String>,
    tagsListSize: Int
  ): Int

}