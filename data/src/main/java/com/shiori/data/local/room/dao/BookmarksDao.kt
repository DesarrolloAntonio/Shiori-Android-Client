package com.shiori.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiori.data.local.room.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarksDao {

  @Query("SELECT * FROM bookmarks")
  fun getAll(): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(bookmarks: List<BookmarkEntity>)

  @Query("DELETE FROM bookmarks")
  suspend fun deleteAll()


}