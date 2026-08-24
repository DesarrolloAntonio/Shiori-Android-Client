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

    /**
     * Cached article text is per account. Bookmark ids restart per server, so leaving these behind
     * at logout means the next person to sign in can be served the previous one's article for the
     * same id, which is exactly what the offline fallback does when it cannot reach the server.
     */
    @Query("DELETE FROM bookmark_html")
    suspend fun deleteAll()

    /** Nothing else ever removes a row here, so the cache only grows. Sync prunes it. */
    @Query("DELETE FROM bookmark_html WHERE id NOT IN (SELECT id FROM bookmarks)")
    suspend fun deleteOrphanedHtml()
}