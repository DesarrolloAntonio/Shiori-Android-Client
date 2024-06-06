package com.desarrollodroide.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.desarrollodroide.data.local.room.entity.BookmarkHtmlEntity

@Dao
interface BookmarkHtmlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(bookmarkHtml: BookmarkHtmlEntity)

    @Query("SELECT readableContentHtml FROM bookmark_html WHERE id = :bookmarkId")
    suspend fun getHtmlContent(bookmarkId: Int): String?

    @Query("SELECT * FROM bookmark_html WHERE id = :bookmarkId")
    suspend fun getBookmarkHtml(bookmarkId: Int): BookmarkHtmlEntity?
}